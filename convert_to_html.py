#!/usr/bin/env python3
import sys
import re

def markdown_to_html(md_file, html_file):
    with open(md_file, 'r', encoding='utf-8') as f:
        content = f.read()
    
    html = """<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Feasibility Report</title>
    <style>
        @page {
            size: A4;
            margin: 2cm;
        }
        body {
            font-family: 'Arial', sans-serif;
            line-height: 1.6;
            color: #333;
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
        }
        h1 {
            font-size: 24px;
            color: #1a1a1a;
            border-bottom: 3px solid #333;
            padding-bottom: 10px;
            margin-bottom: 20px;
        }
        h2 {
            font-size: 18px;
            color: #2c3e50;
            margin-top: 30px;
            margin-bottom: 15px;
        }
        h3 {
            font-size: 16px;
            color: #34495e;
            margin-top: 20px;
            margin-bottom: 10px;
        }
        p {
            text-align: justify;
            margin-bottom: 10px;
        }
        ul {
            margin: 10px 0;
            padding-left: 30px;
        }
        li {
            margin-bottom: 8px;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin: 20px 0;
        }
        th, td {
            border: 1px solid #ddd;
            padding: 12px;
            text-align: left;
        }
        th {
            background-color: #4CAF50;
            color: white;
            font-weight: bold;
        }
        tr:nth-child(even) {
            background-color: #f2f2f2;
        }
        img {
            max-width: 100%;
            height: auto;
            margin: 20px 0;
            border: 1px solid #ddd;
        }
        .metadata {
            font-size: 12px;
            color: #666;
            margin-bottom: 20px;
        }
        hr {
            border: none;
            border-top: 2px solid #ddd;
            margin: 20px 0;
        }
        @media print {
            body {
                margin: 0;
                padding: 15px;
            }
            h1 { page-break-after: avoid; }
            h2, h3 { page-break-after: avoid; }
            img { page-break-inside: avoid; }
            table { page-break-inside: avoid; }
        }
    </style>
</head>
<body>
"""
    
    # Parse markdown
    lines = content.split('\n')
    i = 0
    
    while i < len(lines):
        line = lines[i].strip()
        
        # Skip empty lines except for HR
        if not line and i < len(lines) - 1:
            if lines[i + 1].strip().startswith('---'):
                html += "<hr>\n"
                i += 2
                continue
            i += 1
            continue
        
        # Headers
        if line.startswith('# '):
            html += f"<h1>{line[2:]}</h1>\n"
        elif line.startswith('## '):
            html += f"<h2>{line[3:]}</h2>\n"
        elif line.startswith('### '):
            html += f"<h3>{line[4:]}</h3>\n"
        
        # Images
        elif line.startswith('!['):
            match = re.search(r'!\[.*?\]\((.*?)\)', line)
            if match:
                img_path = match.group(1)
                html += f'<img src="{img_path}" alt="Image">\n'
        
        # Tables
        elif '|' in line and 'Item' in line:
            html += "<table>\n"
            # Header
            parts = [p.strip() for p in line.split('|') if p.strip()]
            html += "<tr>"
            for part in parts:
                html += f"<th>{part}</th>"
            html += "</tr>\n"
            # Rows
            i += 1
            while i < len(lines) and '|' in lines[i] and not lines[i].strip().startswith('|--'):
                if lines[i].strip().startswith('|'):
                    parts = [p.strip() for p in lines[i].split('|') if p.strip()]
                    if parts:
                        html += "<tr>"
                        for part in parts:
                            if '**' in part:
                                part = part.replace('**', '<strong>', 1).replace('**', '</strong>', 1)
                            html += f"<td>{part}</td>"
                        html += "</tr>\n"
                i += 1
            html += "</table>\n"
            continue
        
        # Bold metadata lines
        elif line.startswith('**') and '**' in line[2:]:
            text = line.replace('**', '')
            html += f"<p><strong>{text}</strong></p>\n"
        
        # Bullet points
        elif line.startswith('- ') or line.startswith('* '):
            text = line[2:]
            if text:
                html += f"<li>{text}</li>\n"
        
        # Regular paragraphs
        elif line and not line.startswith('**Prepared'):
            text = line
            text = text.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')
            # Handle bold
            text = re.sub(r'\*\*(.*?)\*\*', r'<strong>\1</strong>', text)
            html += f"<p>{text}</p>\n"
        
        # Metadata at start
        elif '**Prepared by:**' in line or '**Department:**' in line or '**Date:**' in line or '**Company:**' in line:
            text = line.replace('**', '')
            html += f'<div class="metadata"><p>{text}</p></div>\n'
        
        i += 1
    
    html += """</body>
</html>"""
    
    with open(html_file, 'w', encoding='utf-8') as f:
        f.write(html)
    
    print(f"HTML created successfully: {html_file}")
    print("Open this file in your browser and print to PDF")

if __name__ == '__main__':
    markdown_to_html('overview.md', 'overview.html')
