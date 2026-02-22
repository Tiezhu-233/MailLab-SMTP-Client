# MailLab SMTP Client

A tiny AWT mail lab where you can press one button and feel like your own mini mail server operator.

What it can do:
- Send email through SMTP.
- Authenticate with `AUTH LOGIN`.
- Upgrade SMTP with `STARTTLS`.
- Pull plain text from an HTTP URL into the message box.

## Quick Start
Requirements:
- JDK 8 (tested with `1.8.0_202`)
- Windows PowerShell / Command Prompt (or any shell with `javac` and `java`)

Build:
```powershell
javac src/main/java/*.java
```

Create local config from template:
```powershell
Copy-Item config.example.properties config.properties
```

Run:
```powershell
java -cp src/main/java EmailClient
```

## Project Layout
- `src/main/java/EmailClient.java`: GUI and app entry point.
- `src/main/java/SMTPInteraction.java`: SMTP protocol flow.
- `src/main/java/HTTPInteraction.java`: Basic HTTP GET implementation.
- `src/main/java/EmailMessage.java`: Email model + validation.

## Catch Outgoing Mail Locally (smtp4dev)
If you want safe testing without sending real emails to real humans, use smtp4dev.

1. Install:
```powershell
winget install --id Rnwood.Smtp4dev.Desktop -e
```
2. Start smtp4dev (Windows `cmd`):
```cmd
start "" "%LOCALAPPDATA%\Microsoft\WinGet\Packages\Rnwood.Smtp4dev.Desktop_Microsoft.Winget.Source_8wekyb3d8bbwe\Rnwood.Smtp4dev.Desktop.exe"
```
3. Open UI: `http://localhost:5000`
4. Set `config.properties` for local capture:
```properties
smtp.host=localhost
smtp.port=25
smtp.starttls=false
smtp.username=
smtp.password=
```

## Configuration
`config.example.properties` is safe to commit.
`config.properties` is local-only and ignored by git.

Defaults are loaded from `config.properties` in project root:
- `smtp.host`, `smtp.port`
- `smtp.starttls` (`true` / `false`)
- `smtp.username`, `smtp.password` (empty means no auth)
- `mail.from`, `mail.to`
- `http.url`

### Real Mailbox Setup
Use provider SMTP settings and app password / authorization code.

Gmail example:
```properties
smtp.host=smtp.gmail.com
smtp.port=587
smtp.starttls=true
smtp.username=yourname@gmail.com
smtp.password=your_16_char_app_password
mail.from=yourname@gmail.com
mail.to=receiver@example.com
```
Notes:
- Use App Password, not account login password.
- Enable 2-Step Verification first.

QQ Mail example:
```properties
smtp.host=smtp.qq.com
smtp.port=587
smtp.starttls=true
smtp.username=yourname@qq.com
smtp.password=your_smtp_authorization_code
mail.from=yourname@qq.com
mail.to=receiver@example.com
```
Notes:
- Enable SMTP service in QQ Mail settings first.
- Use SMTP authorization code, not QQ account password.

## Usage
- Fill SMTP server + port (default `localhost:1025`).
- Fill `From` and `To`.
- Click `Send`.
- Want message content from a URL? Put `example.com/` and click `Get`.
- Sent successfully but not visible? Check Spam/Junk first.

## Limits
- SMTP supports `STARTTLS` and `AUTH LOGIN`.
- HTTP fetch is plain HTTP on port 80 (no HTTPS yet).
- Built for coursework / learning, not production mail ops.

## Troubleshooting
If `javac` works but `java` fails, your `PATH` likely points to a broken Java shim.

Check:
```powershell
java -version
javac -version
```

Fix by moving your JDK `bin` earlier in `PATH`.
