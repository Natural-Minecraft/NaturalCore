from PIL import Image, ImageChops
import os
import math

# Source Image (Absolute Path) - BADGES
SOURCE_IMG = r"C:/Users/USER/.gemini/antigravity/brain/4fc21643-d0f7-4349-ae3f-a3365f226e7a/rank_badges_with_text_concept_1768596075730.png"
OUTPUT_DIR = r"d:\NaturalSMP\plugin\NaturalCore\generated_badges"

if not os.path.exists(OUTPUT_DIR):
    os.makedirs(OUTPUT_DIR)

# Rank Mapping (Row-Major Order)
RANKS = [
    "warrior_badge", "elite_badge", "master_badge",
    "grandmaster_badge", "epic_badge", "legend_badge",
    "mythic_badge", "mythical_glory_badge", "mythical_immortal_badge"
]

def distance(c1, c2):
    return math.sqrt(sum((a - b) ** 2 for a, b in zip(c1, c2)))

def trim_with_tolerance(im, tolerance=30):
    bg = im.getpixel((0, 0))
    width, height = im.size
    
    # Left
    left = 0
    for x in range(width):
        is_bg = True
        for y in range(height):
            if distance(im.getpixel((x, y)), bg) > tolerance:
                is_bg = False
                break
        if not is_bg:
            left = x
            break
            
    # Right
    right = width
    for x in range(width - 1, -1, -1):
        is_bg = True
        for y in range(height):
            if distance(im.getpixel((x, y)), bg) > tolerance:
                is_bg = False
                break
        if not is_bg:
            right = x + 1
            break
            
    # Top
    top = 0
    for y in range(height):
        is_bg = True
        for x in range(width):
            if distance(im.getpixel((x, y)), bg) > tolerance:
                is_bg = False
                break
        if not is_bg:
            top = y
            break
            
    # Bottom
    bottom = height
    for y in range(height - 1, -1, -1):
        is_bg = True
        for x in range(width):
            if distance(im.getpixel((x, y)), bg) > tolerance:
                is_bg = False
                break
        if not is_bg:
            bottom = y + 1
            break
            
    if left < right and top < bottom:
        return im.crop((left, top, right, bottom))
    return im

def slice_sprites():
    try:
        with Image.open(SOURCE_IMG) as img:
            img = img.convert("RGBA")
            width, height = img.size
            cell_w = width // 3
            cell_h = height // 3
            
            print(f"Slicing {width}x{height} image into 3x3 grid (Cell: {cell_w}x{cell_h})...")
            
            idx = 0
            for row in range(3):
                for col in range(3):
                    if idx >= len(RANKS): break
                    
                    left = col * cell_w
                    upper = row * cell_h
                    right = left + cell_w
                    lower = upper + cell_h
                    
                    # Crop
                    sprite = img.crop((left, upper, right, lower))
                    
                    # AUTO-CROP with tolerance
                    sprite = trim_with_tolerance(sprite, tolerance=70)
                    rank_name = RANKS[idx]
                    print(f"DEBUG: Trimmed {rank_name} to {sprite.width}x{sprite.height}")
                    
                    # Resize to height 9px (maintain aspect ratio)
                    target_height = 9
                    aspect_ratio = sprite.width / sprite.height
                    new_width = int(target_height * aspect_ratio)
                    new_width = max(1, new_width)
                    
                    # Lanczos for smooth downscale
                    sprite = sprite.resize((new_width, target_height), Image.Resampling.LANCZOS)
                    
                    # Save
                    rank_name = RANKS[idx]
                    out_path = os.path.join(OUTPUT_DIR, f"{rank_name}.png")
                    sprite.save(out_path)
                    print(f"Saved {rank_name}.png ({new_width}x{target_height})")
                    
                    idx += 1
                    
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    slice_sprites()
