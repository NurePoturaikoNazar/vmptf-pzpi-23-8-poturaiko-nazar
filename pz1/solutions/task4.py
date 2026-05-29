import sys
def quicksort(a):
    if len(a) <= 1:
        return a
    pivot = a[len(a) // 2]
    left = [x for x in a if x < pivot]
    mid = [x for x in a if x == pivot]
    right = [x for x in a if x > pivot]
    return quicksort(left) + mid + quicksort(right)

def read_numbers(prompt):
    s = input(prompt).strip()
    if not s:
        return []
    parts = s.split()
    nums = []
    for p in parts:
        try:
            nums.append(float(p))
        except Exception:
            print("Invalid input", file=sys.stderr)
            sys.exit(1)
    return nums

def main():
    arr = read_numbers("Enter numbers separated by spaces: ")
    sorted_arr = quicksort(arr)
    if all(float(x).is_integer() for x in sorted_arr):
        out = " ".join(str(int(x)) for x in sorted_arr)
    else:
        out = " ".join(str(x) for x in sorted_arr)
    print("Sorted array:", out)

if __name__ == "__main__":
    main()
