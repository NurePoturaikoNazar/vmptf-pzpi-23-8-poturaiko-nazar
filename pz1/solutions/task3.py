import sys
from datetime import date

def read_number (prompt):
    try:
        return int (input(prompt).strip())
    except Exception:
        print ("incorrect input", file=sys.stderr)
        sys.exit(1)

def main():
    day = read_number ("enter day (1-31): ")
    month = read_number ("enter day (1-12): ")
    year = read_number ("enter day (YYYY): ")


    try:
        birth_date = date(year, month, day)
    except ValueError:
        print ("invalid birth date", file=sys.stderr)
        sys.exit(1)

    today = date.today()
    if birth_date > today:
        print ("invalid birth date", file=sys.stderr)
        sys.exit(1)

    age = today.year - birth_date.year
    if (today.month, today.day) < (birth_date.month, birth_date.day):
        age -= 1

    print (f"Age: {age} years")

if __name__ == "__main__":
    main()
