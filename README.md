# GutselCraft
Server Plugin for GutselCraft.de Minecraft Server

# Ultimate Oracle Cloud Minecraft Server Guide

**Architecture:** Ubuntu (ARM) • Paper MC • Crafty Controller • Nginx Proxy Manager • Docker

----------

## Phase 1: VPS & Network Initialization

### 1.1 Oracle Cloud Dashboard (Ingress Rules)

Before touching the terminal, open the ports in the Oracle VCN Security List.

-   **Navigate to:** Networking -> VCN -> Security Lists -> Default Security List -> Ingress Rules.

-   **Add Rule:**

    -   Source: 0.0.0.0/0

    -   Protocol: TCP

    -   Destination Ports: All
-   **Add Rule:**

    -   Source: 0.0.0.0/0

    -   Protocol: UDP

    -   Destination Ports: All


### 1.2 OS Level Firewall (The "Double Lock" Fix)

Oracle Ubuntu often runs both iptables and firewalld. We configure both to be safe.

**A. Configure IPTables (Kernel Level)**
```bash
# Web & Proxy
sudo iptables -I INPUT -p tcp --dport 80 -j ACCEPT
sudo iptables -I INPUT -p tcp --dport 443 -j ACCEPT
sudo iptables -I INPUT -p tcp --dport 81 -j ACCEPT

# Minecraft & Management
sudo iptables -I INPUT -p tcp --dport 25565 -j ACCEPT
sudo iptables -I INPUT -p udp --dport 25565 -j ACCEPT
sudo iptables -I INPUT -p tcp --dport 8443 -j ACCEPT

# Plugins (Voice Chat & Map)
sudo iptables -I INPUT -p udp --dport 24454 -j ACCEPT
sudo iptables -I INPUT -p tcp --dport 8100 -j ACCEPT

# Save Persistence
sudo apt update && sudo apt install iptables-persistent -y
sudo netfilter-persistent save
```

**B. Configure Firewalld (Service Level)**

```bash
sudo systemctl enable firewalld
sudo firewall-cmd --permanent --zone=public --add-port=80/tcp
sudo firewall-cmd --permanent --zone=public --add-port=443/tcp
sudo firewall-cmd --permanent --zone=public --add-port=81/tcp
sudo firewall-cmd --permanent --zone=public --add-port=25565/tcp
sudo firewall-cmd --permanent --zone=public --add-port=25565/udp
sudo firewall-cmd --permanent --zone=public --add-port=8443/tcp
sudo firewall-cmd --permanent --zone=public --add-port=24454/udp
sudo firewall-cmd --permanent --zone=public --add-port=8100/tcp
sudo firewall-cmd --reload
```

----------

## Phase 2: Crafty Controller (Admin Panel)

### 2.1 Installation

```bash
bash <(curl -s https://craftycontrol.com/install)
# Select "Install Crafty" -> Yes to Service -> Yes to Start on Boot
```

-   **Access:**  https://YOUR_IP:8443

-   **Create Server:** Paper 1.21 (Java 21).


### 2.2 Fixing Permissions (Critical for VS Code SSH)

To allow your ubuntu user to edit server files (via VS Code) while letting crafty run the server:

1.  **Add user to group:**

    ```bash
    sudo usermod -aG crafty ubuntu
    ```

    (You must logout/login or reboot for this to apply).

2.  **Install ACL for permanent permissions:**

    ```bash
    sudo apt install acl -y
    ```

3.  **Apply permissions to the server folder:**

    ```bash
    # Set current permissions
    sudo chown -R crafty:crafty /var/opt/minecraft/crafty/crafty-4/servers
    sudo chmod -R 775 /var/opt/minecraft/crafty/crafty-4/servers
    
    # Set "Sticky Bit" (New files inherit group)
    sudo find /var/opt/minecraft/crafty/crafty-4/servers -type d -exec chmod g+s {} +
    
    # Set ACL Default (New files inherit Read/Write for group)
    sudo setfacl -R -d -m g::rwx /var/opt/minecraft/crafty/crafty-4/servers
    sudo setfacl -R -m g::rwx /var/opt/minecraft/crafty/crafty-4/servers
    ```


----------

## Phase 3: Minecraft Server Configuration

### 3.1 Anti-Xray (Paper Built-in)

Edit config/paper-world-defaults.yml:

```yml
anticheat:
  anti-xray:
    enabled: true
    engine-mode: 2  # Fake Ores mode
```

----------

## Phase 4: Plugins Setup

### 4.1 Simple Voice Chat

-   **Download:** Paper/Bukkit version.

-   **Config:**  plugins/voicechat/voicechat-server.properties

-   **Port:**  port=24454 (Default UDP).

-   **Firewall:** Ensure UDP 24454 is open (done in Phase 1).


### 4.2 BlueMap (3D Web Map)

-   **Download:** Paper/Bukkit version.

-   **Config:**  plugins/BlueMap/core.conf -> accept-download: true.

-   **Port:** Default is 8100.

-   **Render:** Run /bluemap render world in console.


### 4.3 Chunky (Pre-Generation)

Prevent lag by pre-generating the world.

```bash
# Overworld
chunky world world
chunky radius 5000
chunky start

# Nether
chunky world world_nether
chunky radius 4000
chunky start

# The End
chunky world world_the_end
chunky radius 5000
chunky start
```

----------

## Phase 5: Domains & Reverse Proxy

### 5.1 Docker & Nginx Proxy Manager

1.  **Install Docker:**

    ```bash
    curl -fsSL https://get.docker.com -o get-docker.sh && sudo sh get-docker.sh
    ```

2.  **Create Setup:**

    ```bash
    mkdir ~/proxy && cd ~/proxy && nano docker-compose.yml
    ```

3.  **Compose File:**

    ```yml
    version: '3.8'
    services:
      app:
        image: 'jc21/nginx-proxy-manager:latest'
        restart: unless-stopped
        ports:
          - '80:80'
          - '81:81'
          - '443:443'
        volumes:
          - ./data:/data
          - ./letsencrypt:/etc/letsencrypt
    ```

4.  **Run:**
    ```bash
    sudo docker compose up -d
    ```

## 5.2 DNS (Porkbun)
Set **A Records** pointing to Oracle IP: `(Root)`, `map/karte`, `admin`, `proxy`.

### 5.3 Proxy Host Configuration
Login: `IP:81`.
**Forward Host IP:** `172.17.0.1`

| Domain | Scheme | Forward Port | SSL |
| :--- | :--- | :--- | :--- |
| `admin.gutselcraft.de` | `https` | `8443` | Force SSL |
| `karte.gutselcraft.de` | `http` | `8100` | Force SSL |
| `proxy.gutselcraft.de` | `http` | `81` | Force SSL |
| `gutselcraft.de` | `http` | `8080` | Force SSL |

---

## Phase 6: Website Hosting (CI/CD)

### 6.1 Website Container
Update `~/proxy/docker-compose.yml` to include the website:
```yaml
  website:
    image: nginx:alpine
    container_name: gutsel_website
    restart: unless-stopped
    volumes:
      - /home/ubuntu/website/html:/usr/share/nginx/html
    ports:
      - "8080:80"
```

### 6.2 GitHub Actions (Auto-Deploy)
1.  **Generate SSH Key:**
    ```bash
    ssh-keygen -t ed25519 -f ~/.ssh/github_deploy
    cat ~/.ssh/github_deploy.pub >> ~/.ssh/authorized_keys
    ```
2.  **GitHub Secrets:** `SSH_PRIVATE_KEY`, `SSH_HOST`, `SSH_USER`.
3.  **Workflow (`.github/workflows/deploy.yml`):**
    ```yaml
    name: Deploy
    on: [push]
    jobs:
      deploy:
        runs-on: ubuntu-latest
        steps:
          - uses: actions/checkout@v3
          - uses: appleboy/scp-action@master
            with:
              host: ${{ secrets.SSH_HOST }}
              username: ${{ secrets.SSH_USER }}
              key: ${{ secrets.SSH_PRIVATE_KEY }}
              source: "."
              target: "/home/ubuntu/website/html"
    ```