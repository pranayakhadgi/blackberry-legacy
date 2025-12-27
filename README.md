# Project BlackBerry: Minimal Productivity Terminal on Legacy Hardware

## What This Is

This started as a curiosity experiment: **could my old BlackBerry Z10 sitting in a drawer actually be useful?**

The answer: sort of, but not really. This project is a working MVP that demonstrates the concept, but I intentionally stopped here once the core limitations became clear.

## The Experiment

The idea was simple: use a legacy BlackBerry Z10 as a minimal productivity terminal by offloading all computation to a Java HTTP server running on my laptop. The phone would act purely as a thin web client—no apps, no distractions, just a simple interface for checklists and curated resources.

### What Works

- ✅ Z10 browser successfully connects to laptop over WiFi
- ✅ Java server serves HTML pages optimized for BB10's old WebKit browser
- ✅ Weekly checklist system with localStorage persistence
- ✅ Resource navigator with curated links
- ✅ File-based persistence (checklists saved to disk)

### What Doesn't (And Why I Stopped)

The original goal was to have a "Linux-like" setup on the phone for minimal productivity. But here's the thing: implementing that would reintroduce the same complexity and distractions I was trying to avoid. The simplest viable setup—Z10 as a web client to a laptop—already requires discipline and setup effort.

Pushing further toward "Linux everywhere" began to contradict the simplicity goals. So I stopped at a working MVP rather than over-engineer something that wouldn't actually help.

## Architecture

```
┌─────────────────────┐       HTTP        ┌──────────────────────┐
│ BlackBerry Z10     │  <─────────────> │ Pop!_OS Laptop       │
│ (BB10 Browser)     │                  │ (Java 17 Server)     │
│                     │                  │ Port 8080            │
└─────────────────────┘                  └──────────────────────┘
```

- **Z10**: Legacy BlackBerry 10 browser, connects via WiFi
- **Laptop**: Java HTTP server using built-in `HttpServer` (no frameworks)
- **Communication**: Simple HTTP, HTML/CSS optimized for old WebKit

## Setup

### Requirements

- Java 17+
- Maven
- BlackBerry Z10 on same WiFi network as laptop

### Build & Run

```bash
# Build
mvn clean package

# Run
java -jar target/project-blackberry-server-1.0-SNAPSHOT.jar
```

### Access from Z10

1. Find your laptop's IP: `ip addr` or `hostname -I`
2. On Z10 browser, go to: `http://<laptop-ip>:8080`
3. Bookmark it for easy access

## Features

### Endpoints

- `GET /` - Home page with navigation
- `GET /checklist` - Weekly checklist (auto-detects current week)
- `GET /checklist?week=2025-W1` - Specific week
- `GET /navigator` - Curated resource links
- `GET /setup` - Setup guide
- `POST /import?week=2025-W1` - Import weekly plan JSON

### Weekly Checklist

- Tasks with priorities (CRITICAL, HIGH, MEDIUM, LOW)
- Time estimates per task
- Checkbox state persists via localStorage
- Resources section with curated links

### Import Weekly Plan

```bash
# Edit your plan
nano data/weekly-plan-2025-W1.json

# Import it
./import-weekly.sh 2025-W1
```

## Code Structure

```
src/main/java/com/projectblackberry/
├── ServerMain.java          # HTTP server and handlers
├── ChecklistStorage.java    # File persistence
├── WeekUtils.java           # Week ID utilities
└── model/
    ├── WeeklyChecklist.java
    ├── DayChecklist.java
    ├── TaskItem.java
    └── ResourceLink.java
```

Uses Java's built-in `HttpServer`—no Spring Boot or other frameworks. Just plain Java 17.

## Why This Exists

Honestly? I was curious if an old phone could be repurposed. The answer is "technically yes, but practically no." The BB10 browser is too limited, and the setup complexity defeats the purpose of minimalism.

This project serves as:
- A working proof-of-concept
- A learning exercise in networking and Java HTTP servers
- A reminder that sometimes the simplest solution is to stop when you've proven the concept

## Limitations

- BB10 browser struggles with modern web features
- Requires laptop to be running and on same network
- No offline functionality
- Limited by legacy hardware constraints

## Future Directions (If Anyone Cares)

- Port to PinePhone or similar open hardware
- Use a more capable device with better browser
- Or just accept that modern phones exist for a reason

## License

MIT (or whatever you prefer—it's your code)

---

**TL;DR**: Built a working prototype to see if an old phone could be useful. It works, but the limitations made it clear this wasn't the right approach. Stopped at MVP. Here's the code.

