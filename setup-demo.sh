#!/bin/bash

# GitHub Copilot Tax Demo Setup Script
# Run this before your demo to ensure everything is ready

echo "🚀 Setting up GitHub Copilot Tax Modernization Demo..."
echo "📅 Demo Date: $(date)"
echo ""

# Check if we're in the right directory
if [ ! -f "legacy-cobol/TAX-CALC.cbl" ]; then
    echo "❌ Error: Please run this script from the COBOLTest directory"
    exit 1
fi

echo "✅ Demo files found"

# Display file structure
echo ""
echo "📁 Demo structure:"
if command -v tree >/dev/null 2>&1; then
    tree . -I '.git'
else
    find . -type f -not -path "./.git/*" | sort
fi

echo ""
echo "🔍 Checking demo files..."

# Check each required file
files=(
    "legacy-cobol/TAX-CALC.cbl"
    "modern-java/TaxCalculationService.java" 
    "modern-java/TaxDataService.java"
    "docs/FOOLPROOF-DEMO-SCRIPT.md"
    "docs/DEMO-CHEAT-SHEET.md"
    "docs/README.md"
)

for file in "${files[@]}"; do
    if [ -f "$file" ]; then
        echo "✅ $file"
    else
        echo "❌ Missing: $file"
    fi
done

echo ""
echo "🎯 QUICK DEMO TEST:"
echo "1. Open VS Code in this folder"
echo "2. Press Cmd+Shift+I (Mac) or Ctrl+Shift+I (Windows)"
echo "3. Type: 'Hello, are you working?'"
echo "4. You should get a Copilot response"
echo ""

echo "📋 PRE-DEMO CHECKLIST:"
echo "   □ VS Code open with this workspace"
echo "   □ GitHub Copilot extension active (green checkmark)"
echo "   □ Copilot Chat tested and working"
echo "   □ Cheat sheet printed (docs/DEMO-CHEAT-SHEET.md)"
echo "   □ Full script reviewed (docs/FOOLPROOF-DEMO-SCRIPT.md)"
echo "   □ Browser ready with GitHub repo (optional)"
echo ""

echo "🎬 DEMO READY FOR DAVID ROSEBERRY!"
echo ""
echo "💡 Quick tips:"
echo "   - Print the cheat sheet and keep it visible"
echo "   - Practice the opening line at least once"
echo "   - Remember: You're solving THEIR exact problem"
echo "   - Stay confident - the demo is bulletproof!"
echo ""
echo "🚀 Good luck with your presentation!"