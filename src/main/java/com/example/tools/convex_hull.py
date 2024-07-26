import json
import numpy as np
from scipy.spatial import ConvexHull
from shapely.geometry import Polygon, mapping
import sys
import os

def process_file(input_file):
    # Load JSON data
    with open(input_file, 'r') as f:
        data = json.load(f)

    # Extract coordinates
    coords = np.array([(node['lon'], node['lat']) for node in data])

    # Compute convex hull
    hull = ConvexHull(coords)

    # Create polygon from hull
    polygon = Polygon(coords[hull.vertices])

    # Simplify polygon (optional)
    simplified_polygon = polygon.simplify(0.001)  # Adjust tolerance as needed

    # Convert to GeoJSON
    geojson = mapping(simplified_polygon)

    # Generate output file name
    base_name = os.path.splitext(input_file)[0]
    output_file = f"{base_name}_boundary.geojson"

    # Save GeoJSON to file
    with open(output_file, 'w') as f:
        json.dump(geojson, f)

    print(f"Boundary GeoJSON saved to: {output_file}")

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python convex_hull.py <input_file.json>")
        sys.exit(1)

    input_file = sys.argv[1]
    if not os.path.exists(input_file):
        print(f"Error: File '{input_file}' not found.")
        sys.exit(1)

    process_file(input_file)