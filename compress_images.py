import os
from PIL import Image

def compress_images():
    source_dir = r"d:\NaturalSMP\plugin\NaturalCore\public"
    output_dir = os.path.join(source_dir, "compressed_9px")
    
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)
    
    target_height = 9
    
    print(f"Processing images in {source_dir}...")
    print(f"Target Height: {target_height}px (RGBA 32-bit)")
    
    for filename in os.listdir(source_dir):
        if filename.lower().endswith(('.png', '.jpg', '.jpeg', '.webp')):
            file_path = os.path.join(source_dir, filename)
            
            try:
                with Image.open(file_path) as img:
                    # Convert to RGBA (32-bit)
                    img = img.convert("RGBA")
                    
                    # Calculate new width to maintain aspect ratio
                    aspect_ratio = img.width / img.height
                    new_width = int(target_height * aspect_ratio)
                    
                    # Ensure width is at least 1
                    new_width = max(1, new_width)
                    
                    # Resize (LANCZOS for quality)
                    img_resized = img.resize((new_width, target_height), Image.Resampling.LANCZOS)
                    
                    # Save to compressed folder
                    output_path = os.path.join(output_dir, filename)
                    img_resized.save(output_path, "PNG", optimize=True)
                    
                    # Get sizes
                    original_size = os.path.getsize(file_path) / 1024 # KB
                    new_size = os.path.getsize(output_path) / 1024 # KB
                    
                    print(f"[Done] {filename}: {original_size:.2f}KB -> {new_size:.2f}KB ({new_width}x{target_height})")
            except Exception as e:
                print(f"[Error] Failed to process {filename}: {e}")

if __name__ == "__main__":
    compress_images()
