#!/usr/bin/env python3
"""
Script to analyze missal.json and identify missing liturgical years (A/B/C) for Sunday readings.
"""

import json
from collections import defaultdict

def main():
    # Load the missal.json file
    with open('app/src/main/assets/missal.json', 'r') as f:
        data = json.load(f)
    
    # Track missing years for each date
    missing_years = defaultdict(list)
    
    # Expected years for Sunday readings
    expected_years = {'A', 'B', 'C'}
    
    # Analyze each date
    for date, content in data['data'].items():
        # Only check dates that have a Sunday section (i.e., can be Sundays)
        if 'Sunday' in content:
            available_years = set(content['Sunday'].keys())
            missing = expected_years - available_years
            if missing:
                missing_years[date].extend(sorted(missing))
        # Note: Dates without Sunday sections are weekdays and don't need Sunday readings
    
    # Sort dates chronologically
    # Parse dates to sort them properly
    def date_sort_key(date_str):
        month, day = date_str.split()
        months = ['January', 'February', 'March', 'April', 'May', 'June', 
                  'July', 'August', 'September', 'October', 'November', 'December']
        return (months.index(month), int(day))
    
    sorted_dates = sorted(missing_years.keys(), key=date_sort_key)
    
    # Print results
    print("Missing liturgical years (A/B/C) for Sunday readings:")
    print("=" * 60)
    for date in sorted_dates:
        missing = missing_years[date]
        # Format like "september 17: missing year b"
        date_lower = date.lower()
        missing_str = ', '.join([f"year {year.lower()}" for year in missing])
        print(f"{date_lower}: missing {missing_str}")
    
    print(f"\nTotal dates with missing years: {len(missing_years)}")

if __name__ == '__main__':
    main()
