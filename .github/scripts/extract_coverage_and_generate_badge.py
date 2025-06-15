import xml.etree.ElementTree as ET
import json
import os

JACOCO_XML_PATH = "build/reports/jacoco/test/jacocoTestReport.xml"
BADGE_OUTPUT_PATH = "../badges/coverage.json"

def extract_coverage(xml_path):
    # print current working dire
    print(f"extract_coverage working directory: {os.getcwd()}")
    tree = ET.parse(xml_path)
    root = tree.getroot()
    for counter in root.findall("counter"):
        if counter.attrib.get("type") == "INSTRUCTION":
            missed = int(counter.attrib["missed"])
            covered = int(counter.attrib["covered"])
            total = missed + covered
            if total == 0:
                return 0.0
            return round((covered / total) * 100, 1)
    return 0.0

def coverage_color(coverage):
    if coverage >= 90:
        return "brightgreen"
    elif coverage >= 75:
        return "yellowgreen"
    elif coverage >= 60:
        return "yellow"
    else:
        return "red"

def generate_badge_json(coverage, output_path):
    badge = {
        "schemaVersion": 1,
        "label": "coverage",
        "message": f"{coverage:.1f}%",
        "color": coverage_color(coverage)
    }
    print(f"Generated badge: {badge}")
    print(f"generate_badge_json working directory: {os.getcwd()}")
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    with open(output_path, "w") as f:
        json.dump(badge, f)

def main():
    coverage = extract_coverage(JACOCO_XML_PATH)
    print(f"Extracted coverage: {coverage}%")
    generate_badge_json(coverage, BADGE_OUTPUT_PATH)

if __name__ == "__main__":
    main()
