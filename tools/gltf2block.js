const fs=require('fs');
const CT={5120:['Int8',1],5121:['UInt8',1],5122:['Int16',2],5123:['UInt16',2],5125:['UInt32',4],5126:['Float',4]};
const NC={SCALAR:1,VEC2:2,VEC3:3,VEC4:4};
function decodeBuffer(g){const u=g.buffers[0].uri;return Buffer.from(u.slice(u.indexOf(',')+1),'base64');}
function read(g,buf,idx){
  const a=g.accessors[idx],bv=g.bufferViews[a.bufferView];
  const [kind,sz]=CT[a.componentType],n=NC[a.type];
  const base=(bv.byteOffset||0)+(a.byteOffset||0),stride=bv.byteStride||(sz*n),out=[];
  for(let i=0;i<a.count;i++){const row=[];
    for(let c=0;c<n;c++){const o=base+i*stride+c*sz;
      row.push(kind==='Float'?buf.readFloatLE(o):kind==='UInt16'?buf.readUInt16LE(o):kind==='UInt32'?buf.readUInt32LE(o):kind==='UInt8'?buf.readUInt8(o):kind==='Int16'?buf.readInt16LE(o):buf.readInt8(o));}
    out.push(n===1?row[0]:row);}
  return out;
}
// --- minimal mat4 (column-major, like glTF) ---
const ident=()=>[1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1];
function mul(a,b){const o=new Array(16).fill(0);
  for(let c=0;c<4;c++)for(let r=0;r<4;r++){let s=0;for(let k=0;k<4;k++)s+=a[k*4+r]*b[c*4+k];o[c*4+r]=s;}
  return o;}
function fromTRS(t,q,s){
  t=t||[0,0,0]; q=q||[0,0,0,1]; s=s||[1,1,1];
  const [x,y,z,w]=q;
  const m=[
    (1-2*(y*y+z*z))*s[0], (2*(x*y+z*w))*s[0], (2*(x*z-y*w))*s[0], 0,
    (2*(x*y-z*w))*s[1], (1-2*(x*x+z*z))*s[1], (2*(y*z+x*w))*s[1], 0,
    (2*(x*z+y*w))*s[2], (2*(y*z-x*w))*s[2], (1-2*(x*x+y*y))*s[2], 0,
    t[0], t[1], t[2], 1];
  return m;
}
function apply(m,p){return [
  m[0]*p[0]+m[4]*p[1]+m[8]*p[2]+m[12],
  m[1]*p[0]+m[5]*p[1]+m[9]*p[2]+m[13],
  m[2]*p[0]+m[6]*p[1]+m[10]*p[2]+m[14]];}
function matToEuler(m){ // rotation part -> XYZ euler degrees
  const sy=Math.hypot(m[0],m[1]);
  let X,Y,Z;
  if(sy>1e-6){X=Math.atan2(m[6],m[10]);Y=Math.atan2(-m[2],sy);Z=Math.atan2(m[1],m[0]);}
  else{X=Math.atan2(-m[9],m[5]);Y=Math.atan2(-m[2],sy);Z=0;}
  return [X,Y,Z].map(r=>r*180/Math.PI);
}
function isAxisAligned(m){
  const cols=[[m[0],m[1],m[2]],[m[4],m[5],m[6]],[m[8],m[9],m[10]]];
  return cols.every(c=>c.filter(v=>Math.abs(v)>1e-3).length===1);
}
function convert(gltfPath,texSize){
  const g=JSON.parse(fs.readFileSync(gltfPath,'utf8'));
  const buf=decodeBuffer(g);
  const cubes=[];
  const visit=(ni,parent)=>{
    const node=g.nodes[ni];
    const local=node.matrix?node.matrix:fromTRS(node.translation,node.rotation,node.scale);
    const world=mul(parent,local);
    if(node.mesh!==undefined){
      const prim=g.meshes[node.mesh].primitives[0];
      const pos=read(g,buf,prim.attributes.POSITION);
      const uv=read(g,buf,prim.attributes.TEXCOORD_0);
      const idx=read(g,buf,prim.indices);
      const wpos=pos.map(p=>apply(world,p));
      const lmin=[0,1,2].map(a=>Math.min(...pos.map(p=>p[a])));
      const lmax=[0,1,2].map(a=>Math.max(...pos.map(p=>p[a])));
      const wmin=[0,1,2].map(a=>Math.min(...wpos.map(p=>p[a])));
      const wmax=[0,1,2].map(a=>Math.max(...wpos.map(p=>p[a])));
      const aligned=isAxisAligned(world);
      const faces={};
      for(let t=0;t<idx.length;t+=6){
        const vids=[...new Set(idx.slice(t,t+6))];
        if(vids.length!==4)continue;
        const pts=vids.map(v=>pos[v]);
        let ax=-1;
        for(let a=0;a<3;a++){const v=pts[0][a];if(pts.every(p=>Math.abs(p[a]-v)<1e-6)){ax=a;break;}}
        if(ax<0)continue;
        const centre=(lmin[ax]+lmax[ax])/2;
        const sign=pts[0][ax]>centre?1:-1;
        const name=ax===0?(sign>0?'east':'west'):ax===1?(sign>0?'up':'down'):(sign>0?'south':'north');
        const us=vids.map(v=>uv[v][0]*texSize),vs=vids.map(v=>uv[v][1]*texSize);
        faces[name]={u0:Math.min(...us),v0:Math.min(...vs),u1:Math.max(...us),v1:Math.max(...vs)};
      }
      // axis-aligned world box for an unrotated cube; for a rotated one keep the
      // local box plus the world rotation so the caller can approximate it
      const originW=apply(world,[0,0,0]);
      cubes.push({name:node.name,aligned,
        min:aligned?wmin:[0,1,2].map(a=>originW[a]+lmin[a]),
        max:aligned?wmax:[0,1,2].map(a=>originW[a]+lmax[a]),
        origin:originW,
        rotation:aligned?null:matToEuler(world),
        faces});
    }
    (node.children||[]).forEach(c=>visit(c,world));
  };
  const scene=g.scenes[g.scene||0];
  scene.nodes.forEach(n=>visit(n,ident()));
  return cubes;
}
module.exports={convert};
if(require.main===module){
  const c=convert(process.argv[2],Number(process.argv[3]||128));
  const b=[0,1,2].map(a=>[Math.min(...c.map(x=>x.min[a]))*16,Math.max(...c.map(x=>x.max[a]))*16]);
  console.log("cubes:",c.length,"rotated:",c.filter(x=>!x.aligned).length);
  console.log("bounds X",b[0].map(v=>Math.round(v*100)/100),"Y",b[1].map(v=>Math.round(v*100)/100),"Z",b[2].map(v=>Math.round(v*100)/100));
}
