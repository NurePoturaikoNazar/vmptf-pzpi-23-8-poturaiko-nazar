import sys
from datetime import datetime
def main():
    s = input("Enter your birth year (YYYY): ").strip()
    try:
        year = int(s)
    except Exception:
        print("Invalid input", file=sys.stderr)
        sys.exit(1)
    now = datetime.now().year
    age = now - year
    if age < 0:
        print("Invalid birth year", file=sys.stderr)
        sys.exit(1)
    print(f"Age: {age} years")

if __name__ == "__main__":
    main()
