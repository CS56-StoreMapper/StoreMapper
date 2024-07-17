import pickle
import json
import sys

def pickle_to_json(pickle_file, json_file):
    with open(pickle_file, 'rb') as f:
        data = []
        while True:
            try:
                obj = pickle.load(f)
                data.append(obj)
            except EOFError:
                break
    
    with open(json_file, 'w') as f:
        json.dump(data, f)

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python pickle_to_json.py <input_pickle_file> <output_json_file>")
        sys.exit(1)
    
    pickle_to_json(sys.argv[1], sys.argv[2])