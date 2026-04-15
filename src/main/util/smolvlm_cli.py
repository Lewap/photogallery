import sys
import warnings
import logging
from transformers import AutoProcessor, AutoModelForVision2Seq
from PIL import Image

# --- Suppress all warnings/logs ---
warnings.filterwarnings("ignore")
logging.getLogger().setLevel(logging.ERROR)

# --- Check arguments ---
if len(sys.argv) != 3:
    print("Usage: python3 smolvlm_cli.py <image_path> <prompt>")
    sys.exit(1)

image_path = sys.argv[1]
user_prompt = sys.argv[2]

model_name = "HuggingFaceTB/SmolVLM-500M-Instruct"

# --- Load model ---
processor = AutoProcessor.from_pretrained(model_name)
model = AutoModelForVision2Seq.from_pretrained(model_name)

# --- Load image ---
image = Image.open(image_path)

# --- Build chat message ---
messages = [
    {
        "role": "user",
        "content": [
            {"type": "image"},
            {"type": "text", "text": user_prompt}
        ]
    }
]

# --- Apply template ---
prompt = processor.apply_chat_template(
    messages,
    add_generation_prompt=True
)

# --- Prepare inputs ---
inputs = processor(
    text=prompt,
    images=image,
    return_tensors="pt"
)

# --- Generate ---
outputs = model.generate(**inputs, max_new_tokens=100)

# --- Decode ONLY response ---
#response = processor.batch_decode(outputs, skip_special_tokens=True)[0]
decoded = processor.batch_decode(outputs, skip_special_tokens=True)[0]

keyword = "Assistant: "
idx = decoded.find(keyword)
if idx == -1:
    res = decoded.strip()
else:
    res = decoded[idx + len(keyword):].strip()

print(res)

# --- Print ONLY model output ---
#print(response.strip())

#sample call
#python smolvlm_cli.py '/home/lewap/Pictures/karta pamieci/DCIM/Camera/20151003_131607.jpg' "tag this image with 5 comma separated tags describing the key elements like people, type of environment and landscape, animals and other objects"