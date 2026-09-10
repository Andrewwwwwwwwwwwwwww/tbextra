#!/usr/bin/env python3
"""Resizes the Traveler's Backpack Extras key art to the sizes the stores want.

`icon.png` is the original 500x500 render. Stores want 512 (Modrinth) and 400 (CurseForge),
and the jar wants a 128 so the mod has a face in the Mods list. Because the source is a
render rather than pixel art, this resamples with a bilinear filter; nearest neighbour would
alias the fine texture detail badly at 128.

Pure Python, no dependencies.  Run from the repo root:  python branding/icon-source.py
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import png  # noqa: E402  - vendored beside this script

SOURCE = 'branding/icon.png'
GROUND = (0xE9, 0xEA, 0xEC)


def resize(src, size):
    """Bilinear resample to size x size, flattening any transparency onto the ground."""
    sw, sh, rows = src
    out = []
    for y in range(size):
        # Map the destination pixel's centre back into source space.
        fy = min(sh - 1.0, max(0.0, (y + 0.5) * sh / size - 0.5))
        y0 = int(fy)
        y1 = min(sh - 1, y0 + 1)
        wy = fy - y0
        line = []
        for x in range(size):
            fx = min(sw - 1.0, max(0.0, (x + 0.5) * sw / size - 0.5))
            x0 = int(fx)
            x1 = min(sw - 1, x0 + 1)
            wx = fx - x0
            acc = []
            for channel in range(4):
                top = (rows[y0][x0][channel] * (1 - wx)) + (rows[y0][x1][channel] * wx)
                bot = (rows[y1][x0][channel] * (1 - wx)) + (rows[y1][x1][channel] * wx)
                acc.append(top * (1 - wy) + bot * wy)
            alpha = acc[3] / 255.0
            line.append(tuple(round(acc[i] * alpha + GROUND[i] * (1 - alpha)) for i in range(3)))
        out.append(line)
    return out


if __name__ == '__main__':
    art = png.read(SOURCE)
    print('source %dx%d' % (art[0], art[1]))
    os.makedirs('src/main/resources/assets/tbextra', exist_ok=True)
    for path, size in [('branding/icon-modrinth-512.png', 512),
                       ('branding/icon-curseforge-400.png', 400),
                       ('src/main/resources/assets/tbextra/icon.png', 128)]:
        png.write(path, resize(art, size))
        print('wrote %-48s %d x %d' % (path, size, size))
