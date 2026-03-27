"""
将星球图片的纯黑/纯白背景转为透明。
对 R,G,B 均低于黑阈值或均高于白阈值的像素设为透明，保留水彩边缘的渐变。
"""
import os
import sys

try:
    from PIL import Image
except ImportError:
    print("需要安装 Pillow: pip install Pillow")
    sys.exit(1)

ASSETS_DIR = os.path.join(
    os.path.expanduser("~"),
    ".cursor", "projects", "c-Users-13581-Desktop-KeLing3-0", "assets"
)
# 纯黑/近黑像素的阈值，低于则视为背景
BLACK_THRESHOLD = 40
# 纯白/近白像素的阈值，高于则视为背景
WHITE_THRESHOLD = 235


def process_image(path: str) -> None:
    img = Image.open(path).convert("RGBA")
    data = img.getdata()
    new_data = []
    for item in data:
        r, g, b, a = item
        # 若像素接近黑色或接近白色，设为透明
        is_black = r <= BLACK_THRESHOLD and g <= BLACK_THRESHOLD and b <= BLACK_THRESHOLD
        is_white = r >= WHITE_THRESHOLD and g >= WHITE_THRESHOLD and b >= WHITE_THRESHOLD
        if is_black or is_white:
            new_data.append((r, g, b, 0))
        else:
            new_data.append(item)
    img.putdata(new_data)
    img.save(path, "PNG")
    print(f"  OK: {os.path.basename(path)}")


def main():
    abs_dir = os.path.abspath(ASSETS_DIR)
    if not os.path.isdir(abs_dir):
        print(f"目录不存在: {abs_dir}")
        sys.exit(1)
    pngs = [f for f in os.listdir(abs_dir) if f.lower().endswith(".png")]
    print(f"处理 {len(pngs)} 张图片...")
    for name in sorted(pngs):
        process_image(os.path.join(abs_dir, name))
    print("完成。")


if __name__ == "__main__":
    main()
