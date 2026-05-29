import sys
def read_number(prompt):
    try:
        return float(input(prompt).strip())
    except Exception:
        print("Invalid input", file=sys.stderr)
        sys.exit(1)

def main():
    a = read_number("Enter first number: ")
    b = read_number("Enter second number: ")
    c = read_number("Enter third number: ")
    avg = (a + b + c) / 3
    print(f"Average: {avg:.6f}")

if __name__ == "__main__":
    main()
