import sys
import warnings
import logging
import torch
from transformers import AutoProcessor, AutoModelForVision2Seq
from PIL import Image

# --- Suppress all warnings/logs ---
warnings.filterwarnings("ignore")
logging.getLogger().setLevel(logging.ERROR)

# --- Check arguments ---
if len(sys.argv) < 3:
    print("Usage: python smolvlm_vision_cli.py <id1> <image1.jpg> [id2] [image2.jpg] ... [prompt]")
    sys.exit(1)

# The last argument is always the prompt
user_prompt = sys.argv[-1]

# Process arguments in pairs: (id, image), (id, image), ...
# So we take every other argument starting from index 1 (images) and skip the ids
image_paths = []
ids = []

# Extract pairs: odd indices are IDs, even indices are image paths
# We process all pairs except the last one which is the prompt
for i in range(1, len(sys.argv) - 1, 2):
    id_arg = sys.argv[i]
    image_path = sys.argv[i + 1]
    ids.append(id_arg)
    image_paths.append(image_path)

model_name = "HuggingFaceTB/SmolVLM-500M-Instruct"

# --- Check for GPU ---
if torch.cuda.is_available():
    device = torch.device("cuda")
elif hasattr(torch, "xpu") and torch.xpu.is_available():
    device = torch.device("xpu")
else:
    device = torch.device("cpu")
#print(f"Using device: {device}")

# --- Load model ---
processor = AutoProcessor.from_pretrained(model_name)
model = AutoModelForVision2Seq.from_pretrained(model_name)

# Move model to device (GPU or CPU)
model = model.to(device)
model.eval()  # Set to evaluation mode

# Process each image
results = []
for i, (image_path, id_arg) in enumerate(zip(image_paths, ids)):
    try:
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

        # Move inputs to device (GPU or CPU)
        inputs = {k: v.to(device) for k, v in inputs.items()}

        # --- Generate ---
        outputs = model.generate(**inputs, max_new_tokens=20)

        # --- Decode ONLY response ---
        decoded = processor.batch_decode(outputs, skip_special_tokens=True)[0]
        keyword = "Assistant: "
        idx = decoded.find(keyword)
        if idx == -1:
            res = decoded.strip()
        else:
            res = decoded[idx + len(keyword):].strip()

        #results.append(f"{id_arg},{res}")
        print(f"{id_arg},{res}")
    except Exception as e:
        #results.append(f"Error processing {image_path}: {str(e)}")
        print(f"Error processing {image_path}: {str(e)}")

# Print all results
#for result in results:
#    print(result)