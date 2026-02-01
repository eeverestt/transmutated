import json
import os

# Folder to output the JSON files
output_dir = "."
os.makedirs(output_dir, exist_ok=True)

# List of all your tag names
tags = [
    "stones",
    "nether_stones",
    "saplings",
    "logs",
    "leaves",
    "crops",
    "seeds",
    "small_flowers",
    "tall_flowers",
    "dyes",
    "sands",
    "souls",
    "grounds"
]

# Generate JSON files
for tag in tags:
    data = {
        "type": "transmutated:transmutation",
        "ingredient": f"transmutated:{tag}",
        "result": f"transmutated:{tag}"
    }

    # File name matches the tag
    file_path = os.path.join(output_dir, f"{tag}.json")
    with open(file_path, "w") as f:
        json.dump(data, f, indent=2)

    print(f"Created {file_path}")

print("All JSON files generated successfully!")
