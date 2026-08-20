// Reads a Blockbench glTF export into world-space quads with normalised UVs.
// Blockbench bakes group rotations into the node hierarchy, so walking the scene
// graph with full transforms reproduces the model exactly as authored.
const fs=require('fs');
const CT={5120:['Int8',1],5121:['UInt8',1],5122:['Int16',2],5123:['UInt16',2],5125:['UInt32',4],5126:['Float',4]};
const NC={SCALAR:1,VEC2:2,VEC3:3,VEC4:4};
const ident=()=>[1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1];
function mul(a,b){const o=new Array(16).fill(0);
  for(let c=0;c<4;c++)for(let r=0;r<4;r++){let s=0;for(let k=0;k<4;k++)s+=a[k*4+r]*b[c*4+k];o[c*4+r]=s;}return o;}
function fromTRS(t,q,s){t=t||[0,0,0];q=q||[0,0,0,1];s=s||[1,1,1];const[x,y,z,w]=q;return[
  (1-2*(y*y+z*z))*s[0],(2*(x*y+z*w))*s[0],(2*(x*z-y*w))*s[0],0,
  (2*(x*y-z*w))*s[1],(1-2*(x*x+z*z))*s[1],(2*(y*z+x*w))*s[1],0,
  (2*(x*z+y*w))*s[2],(2*(y*z-x*w))*s[2],(1-2*(x*x+y*y))*s[2],0,
  t[0],t[1],t[2],1];}
const apply=(m,p)=>[m[0]*p[0]+m[4]*p[1]+m[8]*p[2]+m[12],m[1]*p[0]+m[5]*p[1]+m[9]*p[2]+m[13],m[2]*p[0]+m[6]*p[1]+m[10]*p[2]+m[14]];
const applyDir=(m,p)=>[m[0]*p[0]+m[4]*p[1]+m[8]*p[2],m[1]*p[0]+m[5]*p[1]+m[9]*p[2],m[2]*p[0]+m[6]*p[1]+m[10]*p[2]];
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
function norm(v){const l=Math.hypot(...v)||1;return v.map(c=>c/l);}
function readGltf(file){
  const g=JSON.parse(fs.readFileSync(file,'utf8'));
  const buf=decodeBuffer(g);
  const quads=[];
  const visit=(ni,parent)=>{
    const node=g.nodes[ni];
    const world=mul(parent,node.matrix?node.matrix:fromTRS(node.translation,node.rotation,node.scale));
    if(node.mesh!==undefined){
      for(const prim of g.meshes[node.mesh].primitives){
        const pos=read(g,buf,prim.attributes.POSITION).map(p=>apply(world,p));
        const uv=read(g,buf,prim.attributes.TEXCOORD_0);
        const nrm=prim.attributes.NORMAL?read(g,buf,prim.attributes.NORMAL).map(n=>norm(applyDir(world,n))):null;
        const idx=read(g,buf,prim.indices);
        // Blockbench emits two triangles per face sharing an edge, e.g. [0,2,1] + [2,3,1].
        // Rebuild the quad by walking the first triangle from its unshared vertex and
        // splicing the second triangle's unshared vertex in across the shared edge, which
        // preserves the original winding.
        for(let t=0;t+5<idx.length;t+=6){
          const tri=idx.slice(t,t+6);
          const A=tri.slice(0,3), B=tri.slice(3,6);
          const uniq=new Set(tri);
          if(uniq.size!==4) continue;
          const d=B.find(v=>!A.includes(v));
          const ui=A.findIndex(v=>!B.includes(v));
          if(d===undefined||ui<0) continue;
          const ring=[A[ui], A[(ui+1)%3], d, A[(ui+2)%3]];
          const P=ring.map(v=>pos[v]);
          let N=nrm?nrm[ring[0]]:null;
          if(!N){
            const e1=[0,1,2].map(a=>P[1][a]-P[0][a]), e2=[0,1,2].map(a=>P[2][a]-P[0][a]);
            N=norm([e1[1]*e2[2]-e1[2]*e2[1], e1[2]*e2[0]-e1[0]*e2[2], e1[0]*e2[1]-e1[1]*e2[0]]);
          }
          quads.push({pos:P, uv:ring.map(v=>uv[v]), normal:N});
        }
      }
    }
    (node.children||[]).forEach(c=>visit(c,world));
  };
  g.scenes[g.scene||0].nodes.forEach(n=>visit(n,ident()));
  return quads;
}
module.exports={readGltf};
