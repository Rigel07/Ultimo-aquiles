#!/usr/bin/env python3
"""
Genera los assets y datos de Achilles: texturas, modelo 3D de la estatua (JSON),
textura y sonidos (OGG sintetizados) del mini Aquiles, lang, worldgen y spawn.

Uso:  python3 tools/generate_assets.py
Necesita: Pillow, numpy y ffmpeg (con libvorbis) para los sonidos.
Si no hay ffmpeg, usa sonidos de vanilla con otro tono como alternativa.
"""
import json
import math
import os
import random
import shutil
import subprocess
import tempfile
import wave

from PIL import Image, ImageDraw

MOD = "achilles"
HERE = os.path.dirname(os.path.abspath(__file__))
RES = os.path.normpath(os.path.join(HERE, "..", "src", "main", "resources"))
ASSETS = os.path.join(RES, "assets", MOD)
DATA = os.path.join(RES, "data", MOD)
rng = random.Random(33)


def wjson(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, ensure_ascii=False, indent=2)


def save_tex(img, rel):
    path = os.path.join(ASSETS, "textures", rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)


def clamp(v):
    return max(0, min(255, int(v)))


def shade(c, f):
    return (clamp(c[0] * f), clamp(c[1] * f), clamp(c[2] * f), 255)


# ============================================================ MINI AQUILES (textura 64x64)
SKIN = (255, 220, 185)
HAIR = (240, 200, 90)
BRONZE = (200, 150, 62)
BRONZE_D = (150, 105, 40)
GOLD = (255, 215, 80)
RED = (190, 35, 45)
TUNIC = (245, 235, 205)
LEATHER = (120, 78, 48)
SILVER = (225, 230, 240)


def faces(u, v, w, h, d):
    return {"top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d),
            "right": (u, v + d, d, h), "front": (u + d, v + d, w, h),
            "left": (u + d + w, v + d, d, h), "back": (u + 2 * d + w, v + d, w, h)}


LIGHT = {"top": 1.05, "bottom": 0.78, "right": 0.92, "left": 0.92, "front": 1.0, "back": 0.88}


def paint_box(img, box, base, noise=3, only=None):
    for name, (x0, y0, rw, rh) in faces(*box).items():
        if only and name not in only:
            continue
        for y in range(rh):
            for x in range(rw):
                n = rng.randint(-noise, noise)
                c = shade(base, LIGHT[name])
                img.putpixel((x0 + x, y0 + y), (clamp(c[0] + n), clamp(c[1] + n), clamp(c[2] + n), 255))


def px(img, x, y, color):
    img.putpixel((x, y), color if len(color) == 4 else color + (255,))


def make_achilles():
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    # piernas, cuerpo, cabeza, brazos
    paint_box(img, (0, 0, 2, 4, 2), SKIN)
    for name, (x0, y0, rw, rh) in faces(0, 0, 2, 4, 2).items():      # sandalias
        if name in ("right", "front", "left", "back"):
            for x in range(rw):
                px(img, x0 + x, y0 + rh - 1, LEATHER)
    paint_box(img, (8, 0, 6, 6, 4), TUNIC)
    for name in ("right", "front", "left", "back"):                   # ribete rojo de la túnica
        x0, y0, rw, rh = faces(8, 0, 6, 6, 4)[name]
        for x in range(rw):
            px(img, x0 + x, y0 + rh - 1, RED)
    paint_box(img, (0, 10, 8, 8, 8), SKIN)
    # pelo dorado: arriba, atrás y laterales
    for name in ("top", "back", "right", "left"):
        x0, y0, rw, rh = faces(0, 10, 8, 8, 8)[name]
        rows = rh if name in ("top", "back") else 4
        for y in range(rows):
            for x in range(rw):
                n = rng.randint(-8, 8)
                c = shade(HAIR, LIGHT[name])
                px(img, x0 + x, y0 + y, (clamp(c[0] + n), clamp(c[1] + n), clamp(c[2] + n), 255))
    fx, fy, fw, fh = faces(0, 10, 8, 8, 8)["front"]                   # cara (8x8 en (8,18))
    for x in range(fw):                                               # flequillo
        px(img, fx + x, fy, HAIR)
        if x in (0, 1, 2, 5, 6, 7):
            px(img, fx + x, fy + 1, HAIR)
    for ex in (1, 5):                                                 # ojazos con brillo
        for dy in range(3):
            for dx in range(2):
                px(img, fx + ex + dx, fy + 3 + dy, (35, 25, 55))
        px(img, fx + ex, fy + 3, (255, 255, 255))
        px(img, fx + ex + 1, fy + 5, (90, 70, 130))
    for bx in (0, 6):                                                 # mofletes
        px(img, fx + bx, fy + 6, (255, 150, 170))
        px(img, fx + bx + 1, fy + 6, (255, 150, 170))
    px(img, fx + 3, fy + 7, (170, 70, 80))                            # sonrisita
    px(img, fx + 4, fy + 7, (170, 70, 80))

    paint_box(img, (0, 26, 2, 5, 2), SKIN)
    for name in ("right", "front", "left", "back"):                   # brazaletes de bronce
        x0, y0, rw, rh = faces(0, 26, 2, 5, 2)[name]
        for y in range(rh - 2, rh):
            for x in range(rw):
                px(img, x0 + x, y0 + y, BRONZE)

    # armadura: peto, casco, cresta, grebas
    paint_box(img, (28, 0, 6, 5, 4), BRONZE)
    bx, by, bw, bh = faces(28, 0, 6, 5, 4)["front"]
    for y in range(bh):
        px(img, bx + 2, by + y, BRONZE_D)
        px(img, bx + 3, by + y, BRONZE_D)
    for x in range(bw):
        px(img, bx + x, by + 2, BRONZE_D)
    px(img, bx + 2, by + 1, GOLD)
    px(img, bx + 3, by + 1, GOLD)
    for name in ("right", "front", "left", "back"):                   # cinturón dorado
        x0, y0, rw, rh = faces(28, 0, 6, 5, 4)[name]
        for x in range(rw):
            px(img, x0 + x, y0 + rh - 1, GOLD)

    paint_box(img, (32, 10, 8, 5, 8), BRONZE, noise=2)
    hx, hy, hw, hh = faces(32, 10, 8, 5, 8)["front"]
    for y in range(hh):                                               # protector nasal
        px(img, hx + 3, hy + y, BRONZE_D)
        px(img, hx + 4, hy + y, BRONZE_D)
    for x in range(hw):
        px(img, hx + x, hy, GOLD)

    paint_box(img, (32, 23, 2, 4, 8), RED, noise=4)
    paint_box(img, (16, 34, 2, 3, 2), BRONZE)

    # escudo: caras grandes con boss dorado
    paint_box(img, (0, 33, 1, 7, 7), BRONZE_D)
    for name in ("right", "left"):
        x0, y0, rw, rh = faces(0, 33, 1, 7, 7)[name]
        for y in range(rh):
            for x in range(rw):
                dist = max(abs(x - 3), abs(y - 3))
                col = GOLD if dist <= 1 else (BRONZE if dist == 2 else BRONZE_D)
                if dist == 3 and (x + y) % 2 == 0:
                    col = BRONZE
                px(img, x0 + x, y0 + y, col)

    # espada (hoja plateada) y guarda dorada
    paint_box(img, (16, 26, 1, 1, 8), SILVER, noise=2)
    paint_box(img, (52, 23, 3, 1, 1), GOLD, noise=2)

    # capa roja con ribete dorado
    paint_box(img, (32, 36, 6, 9, 1), RED, noise=4)
    for name in ("right", "front", "left", "back"):
        x0, y0, rw, rh = faces(32, 36, 6, 9, 1)[name]
        for x in range(rw):
            px(img, x0 + x, y0 + rh - 1, GOLD)
    return img


save_tex(make_achilles(), "entity/achilles.png")


# ============================================================ ESTATUA (textura 64x64 de muestras + modelo JSON)
# Rejilla 4x4 de muestras de 16 px. Celda (col,row) -> uv [col*4, row*4, col*4+4, row*4+4]
SWATCH = {
    "marble": (0, 0), "marble_d": (1, 0), "bronze": (2, 0), "red": (3, 0),
    "gold": (0, 1), "stone": (1, 1), "leather": (2, 1), "dark": (3, 1),
}


def swatch(base, vein=None, noise=6):
    img = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            n = rng.randint(-noise, noise)
            img.putpixel((x, y), (clamp(base[0] + n), clamp(base[1] + n), clamp(base[2] + n), 255))
    if vein:
        x = rng.randint(2, 12)
        for y in range(16):
            x = max(0, min(15, x + rng.choice((-1, 0, 0, 1))))
            img.putpixel((x, y), vein + (255,))
    return img


def make_statue_texture():
    sheet = Image.new("RGBA", (64, 64), (0, 0, 0, 255))
    parts = {
        "marble": swatch((236, 232, 224), (200, 200, 205)),
        "marble_d": swatch((205, 200, 192), (170, 170, 178)),
        "bronze": swatch((178, 126, 54), (210, 160, 80), 8),
        "red": swatch((172, 36, 42), (130, 20, 28), 5),
        "gold": swatch((242, 202, 72), (255, 235, 140), 6),
        "stone": swatch((132, 130, 126), (100, 98, 96), 8),
        "leather": swatch((112, 72, 46), (85, 52, 32), 6),
        "dark": swatch((42, 40, 46), None, 3),
    }
    for name, (col, row) in SWATCH.items():
        sheet.paste(parts[name], (col * 16, row * 16))
    return sheet


save_tex(make_statue_texture(), "block/achilles_statue.png")


def uv(name):
    c, r = SWATCH[name]
    return [c * 4, r * 4, c * 4 + 4, r * 4 + 4]


def cube(x1, y1, z1, x2, y2, z2, mat):
    face = {"uv": uv(mat), "texture": "#t"}
    return {"from": [x1, y1, z1], "to": [x2, y2, z2],
            "faces": {d: dict(face) for d in ("north", "south", "east", "west", "up", "down")}}


# Estatua completa en coordenadas de 2 bloques (0..32 de alto). Mira al norte (-z).
# Se parte en dos modelos: mitad inferior (y 0..16) y superior (y 16..32 -> se baja 16).
LOWER = [
    cube(2, 0, 2, 14, 3, 14, "marble_d"),            # plinto
    cube(3, 3, 3, 13, 4, 13, "stone"),               # escalón
    cube(5, 4, 6, 7.5, 16, 9.5, "marble"),           # pierna izq.
    cube(8.5, 4, 6, 11, 16, 9.5, "marble"),          # pierna der.
    cube(4.8, 4, 5.8, 7.7, 10, 9.7, "bronze"),       # greba
    cube(8.3, 4, 5.8, 11.2, 10, 9.7, "bronze"),
    cube(4.5, 12, 5.5, 11.5, 16, 10.5, "leather"),   # faldellín
    cube(13, 3, 7.5, 14, 16, 8.5, "bronze"),         # lanza (parte baja)
]
UPPER = [
    cube(4.5, 16, 5.5, 11.5, 17.5, 10.5, "gold"),    # cinturón
    cube(4.5, 17.5, 6, 11.5, 26, 10, "marble"),      # torso
    cube(4.4, 18, 5.6, 11.6, 26, 6.2, "bronze"),     # peto
    cube(2.5, 24, 6, 4.5, 27, 10, "bronze"),         # hombreras
    cube(11.5, 24, 6, 13.5, 27, 10, "bronze"),
    cube(2.8, 17.5, 7, 4.5, 24, 9, "marble"),        # brazo izq.
    cube(11.5, 17.5, 7, 13.2, 24, 9, "marble"),      # brazo der. (sujeta la lanza)
    cube(1.5, 17, 4.5, 3, 27, 11.5, "bronze"),       # escudo
    cube(1.2, 21, 7.5, 1.6, 23, 8.5, "gold"),        # emblema del escudo
    cube(13, 16, 7.5, 14, 34, 8.5, "bronze"),        # lanza (parte alta)
    cube(13.05, 34, 7.7, 13.95, 37, 8.3, "gold"),    # punta de la lanza
    cube(7, 26, 7.5, 9, 27.5, 9.5, "marble"),        # cuello
    cube(6, 27, 6, 10, 31.5, 10, "marble"),          # cabeza
    cube(5.7, 29, 5.7, 10.3, 32, 10.3, "bronze"),    # casco
    cube(7.7, 27.5, 5.8, 8.3, 31, 6.1, "bronze"),    # protector nasal
    cube(7.5, 32, 5.5, 8.5, 35.5, 10.5, "red"),      # cresta
    cube(7.3, 30.2, 5.65, 7.9, 30.8, 5.7, "dark"),   # ojos de la estatua
    cube(8.1, 30.2, 5.65, 8.7, 30.8, 5.7, "dark"),
]


def shifted(elements, dy):
    out = []
    for e in elements:
        out.append({"from": [e["from"][0], e["from"][1] - dy, e["from"][2]],
                    "to": [e["to"][0], e["to"][1] - dy, e["to"][2]], "faces": e["faces"]})
    return out


TEXTURES = {"t": f"{MOD}:block/achilles_statue", "particle": f"{MOD}:block/achilles_statue"}
wjson(os.path.join(ASSETS, "models", "block", "achilles_statue_lower.json"),
      {"textures": TEXTURES, "elements": LOWER})
wjson(os.path.join(ASSETS, "models", "block", "achilles_statue_upper.json"),
      {"textures": TEXTURES, "elements": shifted(UPPER, 16)})

variants = {}
for facing, rot in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
    for half in ("lower", "upper"):
        entry = {"model": f"{MOD}:block/achilles_statue_{half}"}
        if rot:
            entry["y"] = rot
        variants[f"facing={facing},half={half}"] = entry
wjson(os.path.join(ASSETS, "blockstates", "achilles_statue.json"), {"variants": variants})


# icono del item de la estatua
def statue_icon():
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    d.rectangle([4, 14, 11, 15], fill=(205, 200, 192, 255))        # plinto
    d.rectangle([6, 8, 9, 14], fill=(236, 232, 224, 255))          # cuerpo
    d.rectangle([5, 8, 10, 10], fill=(178, 126, 54, 255))          # peto
    d.rectangle([6, 4, 9, 7], fill=(236, 232, 224, 255))           # cabeza
    d.rectangle([6, 3, 9, 5], fill=(178, 126, 54, 255))            # casco
    d.rectangle([7, 0, 8, 2], fill=(172, 36, 42, 255))             # cresta
    d.rectangle([2, 7, 4, 12], fill=(178, 126, 54, 255))           # escudo
    d.point((3, 9), fill=(242, 202, 72, 255))
    d.line([(12, 3), (12, 14)], fill=(178, 126, 54, 255))          # lanza
    d.point((12, 2), fill=(242, 202, 72, 255))
    src = im.copy()
    sp, op = src.load(), im.load()
    for y in range(16):
        for x in range(16):
            if sp[x, y][3] == 0:
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < 16 and 0 <= ny < 16 and sp[nx, ny][3] > 0:
                        op[x, y] = (60, 50, 60, 255)
                        break
    return im


save_tex(statue_icon(), "item/achilles_statue.png")
wjson(os.path.join(ASSETS, "models", "item", "achilles_statue.json"),
      {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD}:item/achilles_statue"}})
wjson(os.path.join(ASSETS, "models", "item", "achilles_spawn_egg.json"),
      {"parent": "minecraft:item/template_spawn_egg"})

# ============================================================ SONIDOS CUTE (OGG sintetizados)
SR = 22050


def tone(freq_fn, dur, vib=0.0, harmonics=((1.0, 1.0), (2.0, 0.25)), env="blip"):
    import numpy as np
    n = int(SR * dur)
    t = np.arange(n) / SR
    freq = freq_fn(t / dur)
    phase = 2 * np.pi * np.cumsum(freq + vib * np.sin(2 * np.pi * 18 * t) * freq) / SR
    wave_ = sum(a * np.sin(phase * m) for m, a in harmonics)
    wave_ = wave_ / max(1e-6, sum(a for _, a in harmonics))
    if env == "blip":
        e = np.minimum(1.0, t / 0.01) * np.exp(-3.2 * t / dur)
    else:
        e = np.minimum(1.0, t / 0.02) * np.minimum(1.0, (dur - t) / 0.06)
    return wave_ * e


def silence(dur):
    import numpy as np
    return np.zeros(int(SR * dur))


def concat(*parts):
    import numpy as np
    return np.concatenate(parts)


def write_ogg(samples, name, gain=0.55):
    import numpy as np
    peak = max(1e-6, float(np.max(np.abs(samples))))
    data = (samples / peak * gain * 32767).astype("<i2")
    out_dir = os.path.join(ASSETS, "sounds", "entity")
    os.makedirs(out_dir, exist_ok=True)
    with tempfile.TemporaryDirectory() as tmp:
        wav_path = os.path.join(tmp, "s.wav")
        with wave.open(wav_path, "wb") as w:
            w.setnchannels(1)
            w.setsampwidth(2)
            w.setframerate(SR)
            w.writeframes(data.tobytes())
        subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", wav_path, "-c:a", "libvorbis", "-q:a", "4",
                        os.path.join(out_dir, name + ".ogg")], check=True)


SOUND_EVENTS = {
    "ambient": ["achilles_ambient1", "achilles_ambient2", "achilles_ambient3"],
    "hurt": ["achilles_hurt"],
    "death": ["achilles_death"],
    "trip": ["achilles_trip"],
    "get_up": ["achilles_getup"],
    "clang": ["achilles_clang"],
}
HAVE_FFMPEG = shutil.which("ffmpeg") is not None
try:
    import numpy  # noqa: F401
except ImportError:
    HAVE_FFMPEG = False

if HAVE_FFMPEG:
    import numpy as np

    def up(a, b):          # glissando ascendente
        return lambda x: a + (b - a) * x

    def arc(a, peak, b):   # sube y baja
        return lambda x: a + (peak - a) * np.sin(np.pi * np.minimum(1, x * 1.0)) * (1 - x) + (b - a) * x * 0.0

    # "¡piii!" corto y agudo, tres variantes
    write_ogg(concat(tone(up(900, 1500), 0.11, vib=0.02), silence(0.04), tone(up(1100, 1700), 0.09, vib=0.02)), "achilles_ambient1")
    write_ogg(concat(tone(up(1200, 1800), 0.08), silence(0.03), tone(up(1200, 1800), 0.08), silence(0.03), tone(up(1400, 2000), 0.12, vib=0.03)), "achilles_ambient2")
    write_ogg(tone(lambda x: 1000 + 600 * np.sin(np.pi * x), 0.2, vib=0.03), "achilles_ambient3")
    # "¡eek!": subida rápida y caída
    write_ogg(concat(tone(up(700, 2100), 0.08), tone(up(2100, 1100), 0.12, vib=0.04)), "achilles_hurt")
    # "uwu" triste al morir: bajada lenta
    write_ogg(concat(tone(up(1500, 900), 0.22, vib=0.04), tone(up(900, 450), 0.38, vib=0.05, env="soft")), "achilles_death")
    # tropiezo: "¡uaaa-¡plof!"
    fall = tone(up(1700, 350), 0.45, vib=0.02, env="soft")
    thud = tone(up(160, 60), 0.12, harmonics=((1.0, 1.0),))
    write_ogg(concat(fall, thud), "achilles_trip")
    # levantarse: "¡hup!"
    write_ogg(concat(tone(up(500, 900), 0.07), silence(0.03), tone(up(800, 1400), 0.1)), "achilles_getup")
    # espada de juguete: clang agudo
    write_ogg(tone(up(2600, 2400), 0.18, harmonics=((1.0, 1.0), (2.76, 0.6), (5.4, 0.3))), "achilles_clang")

    sounds = {}
    for event, files in SOUND_EVENTS.items():
        sounds[f"entity.{MOD}.{event}"] = {
            "subtitle": f"subtitles.{MOD}.{event}",
            "sounds": [f"{MOD}:entity/{f}" for f in files]}
else:
    # Alternativa sin ffmpeg: voces de aldeano con tono alto
    fallback = {
        "ambient": [{"name": "minecraft:mob/villager/idle1", "pitch": 1.9, "volume": 0.7},
                    {"name": "minecraft:mob/villager/idle2", "pitch": 1.9, "volume": 0.7}],
        "hurt": [{"name": "minecraft:mob/villager/hit1", "pitch": 1.9}],
        "death": [{"name": "minecraft:mob/villager/death", "pitch": 1.7}],
        "trip": [{"name": "minecraft:mob/villager/no1", "pitch": 1.8}],
        "get_up": [{"name": "minecraft:mob/villager/yes1", "pitch": 1.9}],
        "clang": [{"name": "minecraft:random/anvil_land", "pitch": 2.0, "volume": 0.3}],
    }
    sounds = {f"entity.{MOD}.{k}": {"subtitle": f"subtitles.{MOD}.{k}", "sounds": v} for k, v in fallback.items()}

wjson(os.path.join(ASSETS, "sounds.json"), sounds)

# ============================================================ MUNDO: generación y spawn
wjson(os.path.join(DATA, "worldgen", "configured_feature", "statue.json"),
      {"type": f"{MOD}:statue", "config": {}})
wjson(os.path.join(DATA, "worldgen", "placed_feature", "statue.json"),
      {"feature": f"{MOD}:statue", "placement": [{"type": "minecraft:biome"}]})
wjson(os.path.join(DATA, "forge", "biome_modifier", "add_statues.json"), {
    "type": "forge:add_features",
    "biomes": "#minecraft:is_overworld",
    "features": f"{MOD}:statue",
    "step": "surface_structures"})
wjson(os.path.join(DATA, "forge", "biome_modifier", "add_achilles_spawns.json"), {
    "type": "forge:add_spawns",
    "biomes": ["minecraft:plains", "minecraft:sunflower_plains", "minecraft:forest", "minecraft:birch_forest",
               "minecraft:savanna", "minecraft:meadow"],
    "spawners": {"type": f"{MOD}:achilles", "weight": 3, "minCount": 1, "maxCount": 2}})

# ============================================================ IDIOMAS
ES = {
    "itemGroup.achilles": "Achilles",
    "block.achilles.achilles_statue": "Estatua de Aquiles",
    "item.achilles.achilles_statue": "Estatua de Aquiles",
    "item.achilles.achilles_spawn_egg": "Huevo generador de Aquiles",
    "entity.achilles.achilles": "Aquiles",
    "subtitles.achilles.ambient": "Aquiles hace piii",
    "subtitles.achilles.hurt": "Aquiles se queja",
    "subtitles.achilles.death": "Aquiles se despide",
    "subtitles.achilles.trip": "Aquiles se tropieza",
    "subtitles.achilles.get_up": "Aquiles se levanta",
    "subtitles.achilles.clang": "Aquiles blande su espada",
    "message.achilles.claimed": "¡Has despertado la estatua! Ahora es tuya. Recibes 1 punto de estatua.",
    "message.achilles.owned_by": "Esta estatua pertenece a %s.",
    "message.achilles.trusted": "Esta estatua es de %s, pero tienes permiso para estar aquí.",
    "message.achilles.restricted": "Zona protegida por la estatua de %s. Necesitas su permiso.",
    "message.achilles.player_not_found": "No encuentro al jugador %s.",
    "screen.achilles.title": "Estatua de Aquiles",
    "screen.achilles.owner": "Propietario: %s",
    "screen.achilles.points": "Puntos disponibles: %s",
    "screen.achilles.radius": "Radio: nivel %s/%s",
    "screen.achilles.radius_value": "%s bloques",
    "screen.achilles.heal": "Curación: nivel %s/%s",
    "screen.achilles.heal_value": "%s vida/s",
    "screen.achilles.plus1": "+1",
    "screen.achilles.plus10": "+10",
    "screen.achilles.trusted_title": "Jugadores con permiso",
    "screen.achilles.nobody": "Nadie",
    "screen.achilles.player_name": "Nombre del jugador",
    "screen.achilles.allow": "Permitir",
    "screen.achilles.remove": "Quitar",
}
EN = {
    "itemGroup.achilles": "Achilles",
    "block.achilles.achilles_statue": "Statue of Achilles",
    "item.achilles.achilles_statue": "Statue of Achilles",
    "item.achilles.achilles_spawn_egg": "Achilles Spawn Egg",
    "entity.achilles.achilles": "Achilles",
    "subtitles.achilles.ambient": "Achilles chirps",
    "subtitles.achilles.hurt": "Achilles complains",
    "subtitles.achilles.death": "Achilles says goodbye",
    "subtitles.achilles.trip": "Achilles trips",
    "subtitles.achilles.get_up": "Achilles gets up",
    "subtitles.achilles.clang": "Achilles swings his sword",
    "message.achilles.claimed": "You awakened the statue! It is now yours. You get 1 statue point.",
    "message.achilles.owned_by": "This statue belongs to %s.",
    "message.achilles.trusted": "This statue belongs to %s, but you have permission to be here.",
    "message.achilles.restricted": "Area protected by %s's statue. You need their permission.",
    "message.achilles.player_not_found": "Cannot find player %s.",
    "screen.achilles.title": "Statue of Achilles",
    "screen.achilles.owner": "Owner: %s",
    "screen.achilles.points": "Available points: %s",
    "screen.achilles.radius": "Radius: level %s/%s",
    "screen.achilles.radius_value": "%s blocks",
    "screen.achilles.heal": "Healing: level %s/%s",
    "screen.achilles.heal_value": "%s health/s",
    "screen.achilles.plus1": "+1",
    "screen.achilles.plus10": "+10",
    "screen.achilles.trusted_title": "Players with permission",
    "screen.achilles.nobody": "Nobody",
    "screen.achilles.player_name": "Player name",
    "screen.achilles.allow": "Allow",
    "screen.achilles.remove": "Remove",
}
wjson(os.path.join(ASSETS, "lang", "es_es.json"), ES)
wjson(os.path.join(ASSETS, "lang", "en_us.json"), EN)

print("Assets generados en", RES, "| sonidos:", "OGG sintetizados" if HAVE_FFMPEG else "vanilla (sin ffmpeg)")
