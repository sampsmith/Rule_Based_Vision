#!/usr/bin/env python3
from reportlab.lib.pagesizes import A4
from reportlab.lib.units import mm
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib import colors
from reportlab.lib.enums import TA_LEFT, TA_CENTER, TA_JUSTIFY
import re
from PIL import Image
import os

def markdown_to_pdf(md_file, pdf_file):
    # Read markdown file
    with open(md_file, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Create PDF document
    doc = SimpleDocTemplate(pdf_file, pagesize=A4,
                          rightMargin=20*mm, leftMargin=20*mm,
                          topMargin=30*mm, bottomMargin=20*mm)
    
    # Build content
    elements = []
    
    # Styles
    styles = getSampleStyleSheet()
    
    # Custom styles for UK spelling
    title_style = ParagraphStyle(
        'CustomTitle',
        parent=styles['Heading1'],
        fontSize=20,
        textColor=colors.HexColor('#1a1a1a'),
        spaceAfter=12,
        alignment=TA_LEFT
    )
    
    heading1_style = ParagraphStyle(
        'CustomH1',
        parent=styles['Heading1'],
        fontSize=14,
        textColor=colors.HexColor('#2c3e50'),
        spaceAfter=10,
        spaceBefore=10,
        alignment=TA_LEFT
    )
    
    heading2_style = ParagraphStyle(
        'CustomH2',
        parent=styles['Heading2'],
        fontSize=12,
        textColor=colors.HexColor('#34495e'),
        spaceAfter=8,
        spaceBefore=8,
        alignment=TA_LEFT
    )
    
    normal_style = ParagraphStyle(
        'CustomNormal',
        parent=styles['Normal'],
        fontSize=10,
        textColor=colors.HexColor('#2c3e50'),
        spaceAfter=8,
        alignment=TA_JUSTIFY
    )
    
    # Parse markdown
    lines = content.split('\n')
    i = 0
    in_table = False
    table_data = []
    
    while i < len(lines):
        line = lines[i].strip()
        
        if not line:
            i += 1
            continue
        
        # Check for title (first line with #)
        if i == 0 and line.startswith('# '):
            title = line[2:]
            elements.append(Paragraph(title, title_style))
            i += 1
            continue
        
        # Check for images
        if line.startswith('!['):
            match = re.search(r'!\[.*?\]\((.*?)\)', line)
            if match:
                img_path = match.group(1)
                if os.path.exists(img_path):
                    try:
                        # Add image
                        from reportlab.platypus import Image as RLImage
                        img = RLImage(img_path, width=160*mm, height=120*mm, kind='proportional')
                        elements.append(img)
                        elements.append(Spacer(1, 12))
                    except:
                        pass
            i += 1
            continue
        
        # Check for H1
        if line.startswith('## '):
            text = line[3:]
            elements.append(Paragraph(text, heading1_style))
            i += 1
            continue
        
        # Check for H2
        if line.startswith('### '):
            text = line[4:]
            elements.append(Paragraph(text, heading2_style))
            i += 1
            continue
        
        # Check for horizontal rule
        if line.startswith('---'):
            elements.append(Spacer(1, 12))
            i += 1
            continue
        
        # Check for table
        if '|' in line and not in_table:
            in_table = True
            table_data = []
            # Parse header
            parts = [p.strip() for p in line.split('|') if p.strip()]
            table_data.append(parts)
            i += 1
            continue
        
        if in_table and '|' in line:
            parts = [p.strip() for p in line.split('|') if p.strip()]
            if parts and not all(c == '-' for c in ''.join(parts)):
                table_data.append(parts)
            i += 1
            continue
        elif in_table and line:
            in_table = False
            # Create table
            if table_data:
                num_cols = len(table_data[0])
                t = Table(table_data, colWidths=[45*mm, 80*mm, 25*mm])
                t.setStyle(TableStyle([
                    ('BACKGROUND', (0, 0), (num_cols-1, 0), colors.grey),
                    ('TEXTCOLOR', (0, 0), (num_cols-1, 0), colors.whitesmoke),
                    ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
                    ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
                    ('FONTSIZE', (0, 0), (-1, 0), 10),
                    ('BOTTOMPADDING', (0, 0), (-1, 0), 12),
                    ('BACKGROUND', (0, 1), (-1, -1), colors.beige),
                    ('FONTNAME', (0, 1), (-1, -1), 'Helvetica'),
                    ('FONTSIZE', (0, 1), (-1, -1), 9),
                    ('GRID', (0, 0), (-1, -1), 1, colors.black),
                ]))
                elements.append(t)
                elements.append(Spacer(1, 12))
        
        # Check for bullet points
        if line.startswith('- ') or line.startswith('* '):
            text = line[2:]
            # Escape HTML entities
            text = text.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')
            elements.append(Paragraph(f'&bull; {text}', normal_style))
            i += 1
            continue
        
        # Regular text
        if line and not line.startswith('**') and ':' not in line or '**' in line:
            # Handle bold text
            text = line
            text = text.replace('**', '<b>', 1)
            text = text.replace('**', '</b>', 1)
            text = text.replace('**', '<b>', 1)
            text = text.replace('**', '</b>', 1)
            
            # Escape HTML entities
            text = text.replace('&', '&amp;').replace('<b>', '<b>').replace('</b>', '</b>')
            
            elements.append(Paragraph(text, normal_style))
        
        i += 1
    
    # Handle final table if needed
    if in_table and table_data:
        num_cols = len(table_data[0])
        t = Table(table_data, colWidths=[45*mm, 80*mm, 25*mm])
        t.setStyle(TableStyle([
            ('BACKGROUND', (0, 0), (num_cols-1, 0), colors.grey),
            ('TEXTCOLOR', (0, 0), (num_cols-1, 0), colors.whitesmoke),
            ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
            ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
            ('FONTSIZE', (0, 0), (-1, 0), 10),
            ('BOTTOMPADDING', (0, 0), (-1, 0), 12),
            ('BACKGROUND', (0, 1), (-1, -1), colors.beige),
            ('FONTNAME', (0, 1), (-1, -1), 'Helvetica'),
            ('FONTSIZE', (0, 1), (-1, -1), 9),
            ('GRID', (0, 0), (-1, -1), 1, colors.black),
        ]))
        elements.append(t)
    
    # Build PDF
    doc.build(elements)
    print(f"PDF created successfully: {pdf_file}")

if __name__ == '__main__':
    markdown_to_pdf('overview.md', 'overview.pdf')
