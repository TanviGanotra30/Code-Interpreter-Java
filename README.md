# 🧠 Code Interpreter in Java (with JavaFX UI)

### 🗂️ Overview  
**Code Interpreter** is a mini-language interpreter built completely in **Java**, featuring a custom **lexer**, **parser**, **AST evaluator**, and a **JavaFX-based graphical user interface**.  
It allows users to **write, interpret, and execute custom scripts** directly from a modern, interactive UI — similar to a lightweight IDE.

This project demonstrates strong concepts in **compiler design**, **object-oriented programming**, and **UI integration** using JavaFX.

---

## 🖼️ Preview

> ✨ Here’s a look at the UI you’ll get after running the interpreter!
<img width="1249" height="790" alt="Screenshot 2025-11-01 124802" src="https://github.com/user-attachments/assets/84c95955-bfac-405e-8122-9f1edb103bf0" />
<img width="1252" height="792" alt="Screenshot 2025-11-01 124724" src="https://github.com/user-attachments/assets/ea540b3e-8f72-4392-b766-f584c23df730" />


---

## 🚀 Features

### 🧩 Interpreter Core (`MiniInterpreter.java`)
- 🧠 **Lexical Analysis (Tokenizer):** Converts raw source code into tokens (keywords, identifiers, numbers, etc.)
- 🌲 **Recursive-Descent Parser:** Builds an Abstract Syntax Tree (AST) from tokens.
- ⚙️ **AST Evaluation:** Executes statements like `if`, `while`, `print`, `func`, `return`, etc.
- 🔁 **Function Definitions & Calls:** Supports recursion and local variables.
- 📦 **Symbol Table & Runtime Stack:** Maintains scoped variables and functions.
- 🔍 **CFG Builder:** Generates nodes for control-flow graph analysis (compiler visualization).
- 🚨 **Error Handling:** Reports syntax and runtime issues clearly.

### 🖥️ Graphical Interface (`InterpreterUI.java`)
- Modern **JavaFX-based GUI**
- 📝 **Code Editor:** Write custom scripts
- 💬 **Output Console:** Displays execution results or errors
- 🔘 Buttons for:
  - ▶ **Run Code**
  - 🧹 **Clear Editor**
  - 📂 **Load File**
- Preloads a demo program on startup for instant testing

---

## 💡 Example Code

Here’s a sample program demonstrating recursion, conditionals, and loops:

```c
// Factorial using recursion
func fact(n) {
    if (n == 0) {
        return 1;
    } else {
        return n * fact(n - 1);
    }
}

x = 7;
y = fact(x);
print(y);    // Output: 5040

// Iterative sum
func sumN(n) {
    i = 0;
    acc = 0;
    while (i < n) {
        i = i + 1;
        acc = acc + i;
    }
    return acc;
}

print(sumN(10)); // Output: 55
