<p align="center">
  <img src="https://img.icons8.com/tetris" alt="Tetris Logo" />
</p>

<h1 align="center">🧩 Sehes_TetrisEngine</h1>

<p align="center">
  <strong>A high-performance, modular Tetris engine built with Java & Swing.</strong><br />
  Designed as a technical foundation for a future <b>Tetris-ARPG hybrid</b>.
</p>

<p align="center">
  <a href="https://www.gnu.org/licenses/gpl-3.0" rel="nofollow"><img src="https://camo.githubusercontent.com/48bf9b56d44f38db53ce21294cf0b9487d0a3734ab3ba1fe4c69858ae20db2c1/68747470733a2f2f696d672e736869656c64732e696f2f62616467652f4c6963656e73652d47504c76332d626c75652e737667" alt="License: GPL v3" data-canonical-src="https://img.shields.io/badge/License-GPLv3-blue.svg" style="max-width: 100%;"></a>
  <img src="https://img.shields.io/badge/Java-21-red?style=for-the-badge&logo=java&logoColor=white" alt="Java Version" />
  <img src="https://img.shields.io/badge/Maven-3.9.16-blue?style=for-the-badge&logo=apache-maven&logoColor=white" alt="Maven Version" />
  <img src="https://img.shields.io/badge/Standard-SRS-orange?style=for-the-badge" alt="SRS Standard" />
</p>

---

## 📖 Overview

**TetrisEngine** is more than just a clone; it is a robust implementation focusing on clean OOP principles and game
architecture. By strictly adhering to the **Official Tetris Guidelines** (including the **Super Rotation System**), this
engine provides a smooth and competitive gameplay experience.

The ultimate goal is to evolve this project into a **Tetris-ARPG**, introducing progression, skill trees, and
monster-battling mechanics through row clears.

---

## ✨ Key Features

- 🏗️ **Standard Rotation System (SRS):** Full implementation of wall-kicks and rotation logic.
- ⚡ **Responsive Input:** Low-latency keyboard handling using Swing Key Bindings.
- 📐 **Modular Architecture:** Clean separation of Model, View, and Controller (MVC).
- 🧩 **Standard Tetrominoes:** All 7 official shapes (I, J, L, O, S, T, Z) with correct colors.
- 📊 **Scoring System:** Basic scoring to track your performance.
- ⏸️ **Game Flow:** Integrated Pause and GameOver states.

---

## 🎮 Controls

| Action                       | Input Key     |
|:-----------------------------|:--------------|
| **Start / Pause / Restart**  | `Enter`       |
| **Move Left**                | `Left Arrow`  |
| **Move Right**               | `Right Arrow` |
| **Rotate Clockwise**         | `Up Arrow`    |
| **Rotate Counter-Clockwise** | `A` Key       |
| **Soft Drop**                | `Down Arrow`  |
| **Hard Drop**                | `space`       |
| **GhostBlock Toggle**        | `V` key       |
| **Hold Piece**               | `shift`       |
| **Restart Game**             | `F1`          |

---

## 🗺️ Roadmap

The development journey from a core engine to an ARPG-ready platform.

| ✅ Completed Steps                                        | 🚀 Future & Current Goals                  |
|:----------------------------------------------------------|:-------------------------------------------|
| **Core Mechanics**                                        | **Enhanced Gameplay**                      |
| ✔️ SRS Wall-kick system                                   | ⬜ Auto-increasing difficulty (Gravity)    |
| ✔️ All 7 standard tetrominoes                             |                                            |
| ✔️ Collision detection (Walls & Pieces)                   |                                            |
| ✔️ Basic scoring logic                                    |                                            |
| ✔️ Simple Pause / Game Over system                        |                                            |
| **Enhanced Gameplay**                                     | **Menus & UI**                             | 
| ✔️ Hard drop implementation                               | ⬜ Main Menu                               |  
| ✔️ Advanced Scoring (T-Spins, Combos)                     | ⬜ About & Credits screen                  |
| ✔️ Ghost piece (Visual guide)                             | ⬜ Local Saving (High scores)              |
| ✔️  Preview window for next pieces                        | ⬜ KeyRebinding option                     |
| ✔️  Hold function                                         |                                            |
| ✔️ Standart GuideLine  lock Delay  </br>15 moves / 500 ms |                                            |
| **Technical Foundation**                                  | **Technical Foundation**                   |
| ✔️ Keyboard input handling                                | ⬜ Professional Sprites & Visual Effects   |
| ✔️ Board state & Active tracking                          | ⬜ Separated Threads (Logic vs Render)     |
| ✔️ 2.5D Shape Rendering                                   |                                            |
| ✔️ Game loop (Timer-based)                                |                                            |
| ✔️ Moving & Redrawing logic                               |                                            |
| **ARPG Vision (Planned)**                                 | **Multiplayer & AI**                       |
| 🚧 Modular Refactoring (In Progress)                      | ⬜ Local & Network Multiplayer             |
|                                                           | ⬜ Simple AI Opponent                      |
|                                                           | ⬜ **ARPG:** Monsters, Bosses, Skill Trees |

  
---

## 🛠️ Technical Focus

* **Scalability:** Refactoring the engine to use modular composition, allowing for the easy addition of "ARPG" features
  like skill trees and power-ups.
* **Accuracy:** Ensuring all rotations, wall-kicks, and lock-out timings strictly follow official Tetris guidelines.
* **Performance:** Moving toward a multi-threaded architecture to ensure smooth 60FPS rendering independent of game
  logic calculations.

---

## 🚀 How to Run

### 📋 Prerequisites

* **Java 21** or higher.
* **Maven** (optional, for building from source).

### ⚙️ Execution

1. **Clone the Repo:**
   ```bash
   git clone https://github.com/your-username/JavaSwingTetris.git
   ```
2. **Build with Maven:**
   ```bash
   mvn clean package
   ```
3. **Run the JAR:**
   ```bash
   java -jar target/TetrisEngine.jar
   ```
   *Alternatively, run `org.sehes.tetris.Main` directly from your IDE.*

---

## 🛡️ License

This project is currently available under **GPL v3**. Feel free to explore, learn, and contribute!

---
<p align="center">
  Developed by <b>sehes</b>
</p>
