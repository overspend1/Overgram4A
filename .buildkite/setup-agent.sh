#!/bin/bash
# Buildkite Agent Setup Script for Overgram Android
# Developed by @overspend1
#
# This script sets up a Buildkite agent with Android SDK for building Overgram
# Run this on your build server (Ubuntu/Debian recommended)

set -e

echo "=========================================="
echo "Overgram Android - Buildkite Agent Setup"
echo "=========================================="
echo ""

# Check if running as root
if [ "$EUID" -eq 0 ]; then
   echo "❌ Please do not run as root. Run as your normal user with sudo access."
   exit 1
fi

# Get Buildkite token
echo "📝 Please enter your Buildkite Agent Token:"
echo "   (Get it from: https://buildkite.com/organizations/YOUR_ORG/agents)"
read -p "Token: " BUILDKITE_TOKEN

if [ -z "$BUILDKITE_TOKEN" ]; then
    echo "❌ Token cannot be empty"
    exit 1
fi

echo ""
echo "🔧 Installing dependencies..."
sudo apt-get update
sudo apt-get install -y \
    curl \
    wget \
    unzip \
    git \
    openjdk-17-jdk \
    build-essential

echo ""
echo "📦 Installing Buildkite Agent..."
sudo sh -c 'echo deb https://apt.buildkite.com/buildkite-agent stable main > /etc/apt/sources.list.d/buildkite-agent.list'
sudo apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 32A37959C2FA5C3C99EFBC32A79206696452D198
sudo apt-get update
sudo apt-get install -y buildkite-agent

echo ""
echo "🔑 Configuring agent token..."
sudo sed -i "s/xxx/${BUILDKITE_TOKEN}/g" /etc/buildkite-agent/buildkite-agent.cfg

echo ""
echo "🏷️  Setting agent tags..."
echo 'tags="queue=android,os=linux"' | sudo tee -a /etc/buildkite-agent/buildkite-agent.cfg

echo ""
echo "📱 Installing Android SDK..."
ANDROID_HOME="/home/$USER/android-sdk"
mkdir -p "$ANDROID_HOME"
cd "$ANDROID_HOME"

# Download command line tools
echo "   Downloading Android command line tools..."
wget -q https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip
unzip -q commandlinetools-linux-9477386_latest.zip
rm commandlinetools-linux-9477386_latest.zip

# Organize tools
mkdir -p cmdline-tools/latest
if [ -d "cmdline-tools" ] && [ "$(ls -A cmdline-tools 2>/dev/null | grep -v latest)" ]; then
    mv cmdline-tools/* cmdline-tools/latest/ 2>/dev/null || true
fi

# Accept licenses
echo "   Accepting Android SDK licenses..."
yes | cmdline-tools/latest/bin/sdkmanager --licenses > /dev/null 2>&1

# Install required SDK components
echo "   Installing SDK components..."
cmdline-tools/latest/bin/sdkmanager \
    "platform-tools" \
    "platforms;android-33" \
    "build-tools;33.0.2" \
    "ndk;25.2.9519653" \
    "cmake;3.22.1"

echo ""
echo "🔧 Setting environment variables..."
cat << EOF | sudo tee -a /etc/buildkite-agent/buildkite-agent.cfg
export ANDROID_HOME="$ANDROID_HOME"
export PATH=\$PATH:\$ANDROID_HOME/cmdline-tools/latest/bin:\$ANDROID_HOME/platform-tools:\$ANDROID_HOME/build-tools/33.0.2
export GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.parallel=true -Dorg.gradle.caching=true"
EOF

echo ""
echo "🚀 Starting Buildkite Agent..."
sudo systemctl enable buildkite-agent
sudo systemctl start buildkite-agent

echo ""
echo "✅ Setup complete!"
echo ""
echo "📊 Agent Status:"
sudo systemctl status buildkite-agent --no-pager

echo ""
echo "================================"
echo "🎉 Buildkite Agent is ready!"
echo "================================"
echo ""
echo "Next steps:"
echo "1. Go to https://buildkite.com/organizations/YOUR_ORG/agents"
echo "2. Verify your agent is connected"
echo "3. Trigger a build to test"
echo ""
echo "Useful commands:"
echo "  Check logs:    sudo journalctl -u buildkite-agent -f"
echo "  Restart agent: sudo systemctl restart buildkite-agent"
echo "  Stop agent:    sudo systemctl stop buildkite-agent"
echo ""
echo "Android SDK installed at: $ANDROID_HOME"
echo ""
