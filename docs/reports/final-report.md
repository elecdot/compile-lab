# 编译原理实验最终报告

> Status: working skeleton. This file is the integration point for the final
> submitted report. Replace `TODO` blocks with screenshots, names, dates, and
> final prose before export.

## 报告元信息

| 项目 | 内容 |
| --- | --- |
| 课程 | 编译原理 |
| 实验 | 实验一：词法分析程序的设计与实现；实验二/三：语法制导的三地址代码生成 |
| 小组成员 | TODO：填写成员姓名、学号 |
| 提交内容 | 实验报告、源程序、可执行程序 |
| 可执行程序 | `dist/compiler-lab.jar`，由 `make dist` 生成 |
| 验证命令 | `make test` |
| 最近验证结果 | TODO：填写最终提交前一次 `make test` 结果和日期 |

## 事实源与证据索引

本报告整合三类事实源：

| 类型 | 位置 | 用途 |
| --- | --- | --- |
| 实验指导材料 | `material/lab1.pdf`、`material/lab2-3.pdf`、`material/handbook.pdf` | 实验要求、报告要求、指导书测试样例 |
| 组员报告来源 | `docs/reports/member-sources/lab1.md`、`docs/reports/member-sources/lab2.md` | Lab 1 词法分析与 Lab 2 递归下降语法分析的组员完成情况、设计说明、截图来源 |
| 当前项目实现文档 | `docs/lexer.md`、`docs/parser.md`、`docs/tac.md`、`docs/lab2-3-design-report.md` | 共享词法器、递归下降树输出、Bison 路径、AST/TAC 生成、扩展功能 |
| 自动化测试 | `tests/` | 可复现验证结果 |
| 演示材料 | `examples/` | 报告截图和现场演示输入/输出 |
| 可执行程序 | `dist/compiler-lab.jar` | 提交用可执行产物 |

截图建议统一来自 `examples/` 的输入输出：

| 报告位置 | 示例材料 | 截图占位 |
| --- | --- | --- |
| Lab 1 基本要求 | `examples/lab1/handout.in`、`examples/lab1/handout.out` | TODO：插入词法分析运行截图 |
| Lab 2 基本要求 | `examples/lab2/tree-sample.in`、`examples/lab2/tree-sample.out` | TODO：插入语法树输出截图 |
| Lab 3 基本要求 | `examples/lab3/handout-tac.in`、`examples/lab3/handout-tac.out` | TODO：插入三地址代码输出截图 |
| 扩展关系运算 | `examples/lab3/relop-extended.*` | TODO：插入扩展关系运算截图 |
| 复合语句 | `examples/lab3/compound-while.*` | TODO：插入复合语句 TAC 截图 |
| 错误恢复 | `examples/lab3/syntax-recovery.*`、`examples/lab3/invalid-token-recovery.*` | TODO：插入错误定位与续编译截图 |
| AST 展示 | `examples/lab3/ast-sample.*`、`examples/lab3/ast-dot-sample.*` | TODO：插入 AST 文本或 DOT 图 |
| MiniYacc/SLR | `examples/minislr/table.out`、`examples/minislr/automaton-dot.out` | TODO：插入 SLR 表或自动机截图 |

## 摘要

本项目实现了一个面向编译原理实验要求的微型编译程序。系统以 Java 为主要实现语言，围绕共享词法分析器、递归下降语法树输出、Bison 自动生成语法分析器、AST 中间表示与三地址代码生成器构建完整流程。实验一完成标识符、三类整数、关键字、运算符和非法整数的识别；实验二保留递归下降语法分析路径并输出语法树；实验三使用 GNU Bison 生成语法分析器，构建 AST 后生成三地址代码。

在基本要求之外，项目还实现了全部六种关系运算符、`begin ... end` 复合语句、嵌套控制流、dangling-else 处理、语句级错误定位与续编译、非法 token 恢复、AST 文本与 DOT 展示、常量折叠优化模式，以及固定表达式文法的 MiniYacc/SLR 分析表展示。系统提供 `make dist` 生成可执行 JAR，并通过 `make test` 进行自动化回归验证。

## 1. 实验任务分工

TODO：最终提交前填写真实姓名、学号和分工边界。建议保持事实口径如下：

| 成员 | 负责内容 | 主要产出 | 报告事实源 |
| --- | --- | --- | --- |
| 郑天白 | Lab 1 词法分析 | 词法分析程序设计、正规式/正规文法/状态图、Lab 1 测试截图 | `docs/reports/member-sources/lab1.md` |
| 段晰迈 | Lab 2 递归下降语法分析 | 消除左递归后的产生式、语法图、递归下降 parser、语法树输出、Lab 2 测试截图 | `docs/reports/member-sources/lab2.md` |
| 高子涵 | Bison 路径与 Lab 3 | Bison 文法、AST、TAC 生成、错误恢复、扩展功能、测试和可执行程序 | `src/`、`docs/lab2-3-design-report.md`、`tests/`、`examples/` |
| TODO：全组 | 总体设计、联调、报告整合 | 共享词法器对接、测试验证、最终报告和可执行程序 | 本仓库全部文档与测试 |

## 2. 实验要求与完成情况

| 要求 | 完成情况 | 证据 |
| --- | --- | --- |
| 词法分析：识别标识符、整数、关键字、运算符和分隔符 | 已完成 | `examples/lab1/handout.*`、`tests/lab1_sample.*` |
| 词法分析：给出正规式、正规文法、状态图、主要算法 | 已完成，需在最终稿中补状态图截图 | `docs/reports/member-sources/lab1.md` |
| 语法分析：输出语法树或最左派生产生式序列 | 已完成递归下降树输出 | `examples/lab2/tree-sample.*`、`tests/lab2_tree_sample.*` |
| 语法分析：可处理多个语句 | 已完成 | Lab 2/Lab 3 样例均包含多语句 |
| 语法分析：错误处理 | 已完成基础错误处理；Lab 3 扩展定位与续编译 | `tests/lab2_tree_invalid_octal.*`、`tests/lab3_tac_error_*` |
| 三地址代码生成 | 已完成 | `examples/lab3/handout-tac.*`、`tests/lab3_tac_sample.*` |
| 自动生成技术说明 | 已完成，Lab 3 主线使用 GNU Bison | `src/TacBisonParser.y`、`src/BisonTacParser.java` |
| 可执行程序 | 已完成 | `make dist`、`dist/compiler-lab.jar` |

## 3. 系统总体设计

### 3.1 总体架构

系统包含两条语法分析路径：

```text
Lab 1:
Source -> Lexer -> Token output

Lab 2:
Source -> Lexer -> RecursiveDescentParser -> Syntax tree output

Lab 3:
Source -> Lexer -> Bison-generated parser -> AST -> optional optimizer -> TacEmitter -> CodeGenerator -> TAC
```

其中，`Lexer` 和 `Token` 是共享前端，保证三次实验使用同一套词法规则。Lab 2 保留递归下降实现，满足语法树输出要求；Lab 3 使用 Bison 自动生成 parser，并通过 AST 中间层解耦语法分析和三地址代码生成。

TODO：插入总体架构图。建议图中显示 `Lexer` 共享、Lab 2 递归下降分支、Lab 3 Bison/AST/TAC 分支。

### 3.2 文件与模块

| 模块 | 文件 | 职责 |
| --- | --- | --- |
| 统一可执行入口 | `src/CompilerLab.java` | `java -jar` 子命令分发 |
| Lab 1 入口 | `src/Main.java` | 读取标准输入并输出 token |
| 词法器 | `src/Lexer.java`、`src/Token.java` | 识别 token、保存原始词素和行列号 |
| Lab 2 parser | `src/Parser.java` | 递归下降语法分析和语法树输出 |
| Lab 3 Bison 文法 | `src/TacBisonParser.y` | 自动生成 Lab 3 parser |
| Bison 适配器 | `src/BisonTacParser.java` | 将共享 lexer 适配给 Bison parser |
| AST | `src/TacAst.java` | 保存 Bison 规约后的语法结构 |
| AST 展示 | `src/TacAstPrinter.java`、`src/TacAstDotPrinter.java` | 输出文本 AST 与 Graphviz DOT |
| TAC 生成 | `src/TacEmitter.java`、`src/CodeGenerator.java` | 生成临时变量、标号和三地址代码 |
| 优化 | `src/TacOptimizer.java` | AST 层常量折叠 |
| MiniYacc/SLR | `src/MiniSlrDemo.java` | 固定表达式文法 SLR 表和自动机展示 |

## 4. 词法分析子系统

本节主要整合组员 Lab 1 报告内容，事实源为 `docs/reports/member-sources/lab1.md`，并与当前共享 `Lexer` 实现保持一致。

### 4.1 词法正规式

核心 token 正规式如下：

```text
letter      = A|...|Z|a|...|z
digit       = 0|1|2|3|4|5|6|7|8|9
oct_digit   = 0|1|2|3|4|5|6|7
hex_digit   = digit|a|b|c|d|e|f|A|B|C|D|E|F

IDN         = letter(letter|digit)*
DEC         = 0 | (1|2|3|4|5|6|7|8|9)digit*
OCT         = 0 oct_digit+
HEX         = 0(x|X)hex_digit+
KEYWORD     = if|then|else|while|do|begin|end
OP/DELIM    = +|-|*|/|>|<|=|>=|<=|<>|(|)|;
ILOCT       = 0 digit* (8|9) digit*
ILHEX       = 0(x|X)(digit|letter)* illegal_hex_letter (digit|letter)*
```

当前实现额外允许标识符中出现下划线，因此实现中的标识符规则为：

```text
IDN = (letter|_)(letter|digit|_)*
```

### 4.2 正规文法

TODO：从组员 Lab 1 报告中整理最终版正规文法。建议按以下小节落稿：

- 标识符和关键字的右线性文法；
- 十进制整数的右线性文法；
- 八进制整数与非法八进制整数的右线性文法；
- 十六进制整数与非法十六进制整数的右线性文法；
- 运算符和分隔符的右线性文法。

### 4.3 状态图

TODO：插入词法分析状态图。可直接使用组员 Lab 1 报告中的状态图，或根据 `Lexer.classifyLexeme` / `Lexer.classifyNumber` 重新绘制。

### 4.4 主要数据结构与算法

词法分析器以 `Lexer` 为核心，保存输入串、当前位置、输入长度和当前行列号。每次调用 `nextToken()` 时先跳过空白符，再按以下顺序识别：

1. 优先识别双字符关系运算符 `>=`、`<=`、`<>`；
2. 识别单字符运算符和分隔符；
3. 读取一个非空白、非运算符分隔符的连续词素；
4. 判断是否为关键字、标识符或数字；
5. 对数字进一步区分十进制、八进制、十六进制以及非法整数；
6. 对无法识别的词素返回 `UNKNOWN`。

`Token` 保存 `type`、`value`、`lexeme`、`line`、`column`。其中 `value` 用于语法分析和三地址代码生成，`lexeme` 用于错误信息保留原始输入。

### 4.5 Lab 1 测试结果

基本样例：

```sh
java -jar dist/compiler-lab.jar lab1 < examples/lab1/handout.in
```

TODO：插入运行截图。输出应与 `examples/lab1/handout.out` 一致。

## 5. 语法分析子系统

本项目的语法分析部分包含两个层次：Lab 2 的递归下降语法分析，以及 Lab 3 的 Bison 自动生成语法分析器。最终报告需要同时说明二者关系：递归下降路径用于满足实验二语法树输出要求；Bison 路径用于实验三三地址代码生成主线。

### 5.1 Lab 2 递归下降语法分析

事实源：`docs/reports/member-sources/lab2.md` 与 `src/Parser.java`。

原文法中表达式部分存在左递归：

```text
E -> E + T | E - T | T
T -> T * F | T / F | F
```

消除左递归后：

```text
E  -> T E'
E' -> + T E' | - T E' | ε
T  -> F T'
T' -> * F T' | / F T' | ε
```

语句层面支持：

```text
P      -> L+
L      -> S ;
S      -> id = E
S      -> if C then S S'
S'     -> else S | ε
S      -> while C do S
S      -> begin L_list end
C      -> E relop E
relop  -> > | < | = | >= | <= | <>
F      -> ( E ) | id | int8 | int10 | int16
```

递归下降 parser 为每个主要非终结符设置一个解析函数，通过当前 token 的类型选择产生式。`E'` 和 `T'` 在实现中使用循环处理，从而既保留左结合语义，又避免直接左递归。

TODO：插入组员 Lab 2 报告中的语法图，包括 `S`、`S'`、`C`、`E`、`E'`、`T`、`T'`、`F`。

基本运行命令：

```sh
java -jar dist/compiler-lab.jar tree < examples/lab2/tree-sample.in
```

TODO：插入语法树输出截图。输出应与 `examples/lab2/tree-sample.out` 一致。

### 5.2 Lab 3 Bison 自动生成语法分析器

Lab 3 主路径使用 GNU Bison 生成 Java parser。Bison 文法源文件为 `src/TacBisonParser.y`，构建时由 `make build` 生成 `build/generated/src/TacBisonParser.java`。

核心文法：

```text
program          -> top_statements opt_semi
top_statements   -> statement
                  | top_statements ; statement
statement        -> id = expr
                  | if condition then statement
                  | if condition then statement else statement
                  | while condition do statement
                  | begin compound_statements end
                  | error
compound_stmt    -> statement ;
                  | compound_stmt statement ;
condition        -> expr relop expr
relop            -> > | < | = | >= | <= | <>
expr             -> expr + expr
                  | expr - expr
                  | expr * expr
                  | expr / expr
                  | ( expr )
                  | factor
factor           -> id | int8 | int10 | int16
```

表达式优先级由 Bison 声明给出：

```text
%left ADD SUB
%left MUL DIV
```

`if-then-else` 的 dangling-else 问题通过：

```text
%nonassoc THEN
%nonassoc ELSE
```

以及无 `else` 产生式上的 `%prec THEN` 处理，使 `else` 绑定到最近的未匹配 `if`。

### 5.3 语法分析子系统结构

```text
Lexer -> BisonTacParser.TacLexerAdapter -> TacBisonParser -> TacAst
```

`TacLexerAdapter` 将项目共享 `Token` 映射为 Bison token 编号，并把 token 原始值作为语义值传入 Bison parser。Bison parser 的规约动作不直接生成 TAC，而是构造 AST 节点，后续由 `TacEmitter` 统一翻译。

### 5.4 错误处理

Lab 3 中错误处理分两类：

1. Bison 语法错误通过 `error` 产生式恢复，错误语句构造成 `TacError`；
2. `ILOCT`、`ILHEX`、`ILNUM`、`UNKNOWN` 等非法 token 在 factor 层记录错误，构造 `TacInvalidExpr`，含非法表达式或非法条件的语句最终变成 `TacError`。

`TacEmitter` 会跳过 `TacError`，因此后续合法语句仍能继续生成三地址代码。

运行示例：

```sh
java -jar dist/compiler-lab.jar tac < examples/lab3/invalid-token-recovery.in
```

TODO：插入错误定位与续编译截图。

## 6. 三地址代码生成器

### 6.1 语法制导定义

本项目根据指导书中的语法制导定义生成三地址代码。核心规则如下：

| 产生式 | 翻译规则 |
| --- | --- |
| `S -> id = E` | 先生成 `E.code`，再生成 `id = E.place` |
| `S -> if C then S1` | 生成条件真假跳转，真出口进入 `S1`，假出口到 `S.next` |
| `S -> if C then S1 else S2` | 分别生成 true/false label，两个分支结束后汇合 |
| `S -> while C do S1` | 生成循环入口、条件真出口、条件假出口和回跳 |
| `C -> E1 relop E2` | 生成 `if E1 relop E2 goto C.true` 和 `goto C.false` |
| `E -> E1 op T` | 生成新临时变量保存二元表达式结果 |
| `T -> T1 op F` | 生成新临时变量保存乘除表达式结果 |

### 6.2 算法基本思想

Lab 3 的 TAC 生成采用 AST 后端遍历方式：

```text
TacProgram
  -> TacEmitter.emitProgram
  -> emitStatement / emitCondition / emitExpr
  -> CodeGenerator
```

`CodeGenerator` 负责生成临时变量 `t1`、`t2`、... 和标签 `L0`、`L1`、...，同时保持输出格式与指导书样例接近。表达式翻译返回计算结果所在位置；语句翻译根据控制流需要生成标签和跳转。

基本运行命令：

```sh
java -jar dist/compiler-lab.jar tac < examples/lab3/handout-tac.in
```

TODO：插入指导书 TAC 样例输出截图。

## 7. 扩展内容完成情况

| 扩展 | 实现说明 | 证据 |
| --- | --- | --- |
| 全部关系运算符 | 支持 `>`、`<`、`=`、`>=`、`<=`、`<>` | `examples/lab3/relop-extended.*` |
| 复合语句 | 支持 `begin ... end` 作为多语句块 | `examples/lab3/compound-while.*` |
| 嵌套控制流 | 支持嵌套 `while` 与 `if/else` | `tests/lab3_tac_nested_control.*` |
| Dangling else | Bison 优先级声明使 `else` 绑定最近 `if` | `examples/lab3/dangling-else.*` |
| 错误定位 | 错误信息包含行号、列号、当前 token | `examples/lab3/syntax-recovery.*` |
| 续编译 | 错误语句跳过，后续合法语句继续生成 TAC | `examples/lab3/invalid-token-recovery.*` |
| AST 展示 | `--ast` 输出 Bison 路径 AST 文本 | `examples/lab3/ast-sample.*` |
| AST DOT | `--ast-dot` 输出 Graphviz DOT | `examples/lab3/ast-dot-sample.*` |
| 常量折叠 | `--tac-opt` 在 AST 层折叠常量表达式 | `examples/lab3/constant-folding.*` |
| MiniYacc/SLR 展示 | 固定表达式文法输出 LR(0) 项目集、GOTO、ACTION/GOTO 表 | `examples/minislr/table.out` |

TODO：每个扩展至少保留一张截图或一段输出节选。最终报告可按篇幅选择重点展示 4 到 6 项，其余列入表格。

## 8. 测试与验证

### 8.1 自动化测试

测试入口：

```sh
make test
```

当前测试覆盖：

- Lab 1 指导书样例和共享 lexer 合同；
- Lab 2 递归下降语法树输出和非法八进制错误；
- Lab 3 指导书 TAC 样例、表达式优先级、嵌套控制流；
- 扩展关系运算、复合语句、dangling else；
- 语法错误恢复、缺括号、缺 `then`、块内错误、多错误、非法 token；
- AST 文本、AST DOT；
- 常量折叠；
- MiniYacc/SLR 表和 LR(0) 自动机 DOT；
- 可执行 JAR 的代表性 smoke tests。

TODO：最终提交前填入一次完整测试输出截图或文本节选。

### 8.2 可执行程序

构建命令：

```sh
make dist
```

生成：

```text
dist/compiler-lab.jar
```

常用命令：

```sh
java -jar dist/compiler-lab.jar lab1 < examples/lab1/handout.in
java -jar dist/compiler-lab.jar tree < examples/lab2/tree-sample.in
java -jar dist/compiler-lab.jar tac < examples/lab3/handout-tac.in
java -jar dist/compiler-lab.jar tac-opt < examples/lab3/constant-folding.in
java -jar dist/compiler-lab.jar ast < examples/lab3/ast-sample.in
java -jar dist/compiler-lab.jar minislr
```

TODO：插入 `make dist` 和 `java -jar dist/compiler-lab.jar help` 截图。

## 9. 设计取舍

### 9.1 共享 Lexer

Lab 1、Lab 2、Lab 3 共享同一词法器，避免不同阶段对 token 的解释不一致。这样八进制、十六进制和非法整数的处理只需要在一个位置维护。

### 9.2 Lab 2 保留递归下降，Lab 3 使用 Bison

递归下降实现更适合说明实验二要求中的产生式展开和语法树输出；Bison 自动生成 parser 更适合实验三主线，能够稳定处理优先级、dangling else 和错误恢复。两条路径共享词法器，但分别服务不同实验目标。

### 9.3 Bison Action 构造 AST，不直接生成 TAC

如果在 Bison action 中直接输出 TAC，文法和代码生成会耦合。当前设计先构造 AST，再由 `TacEmitter` 统一生成 TAC，使语法分析、展示、优化和代码生成可以独立测试。

### 9.4 错误恢复边界

当前错误恢复定位在语句级：错误语句不生成 TAC，后续合法语句继续翻译。这样能展示定位和续编译能力，同时避免复杂自动纠错影响正确程序输出。

## 10. 当前限制与后续方向

- 语句级错误恢复尚未实现更细粒度的自动纠错；
- 未实现布尔短路表达式 `and/or/not`；
- 未实现声明、类型检查、数组、函数等语义分析内容；
- MiniYacc/SLR 展示是固定表达式文法，不是完整通用 parser generator；
- 最终报告导出前需要补齐成员姓名、截图和实验体会。

## 11. 实验体会

TODO：最终稿中每位成员可写 1 段。建议覆盖：

- 对词法分析中正规式、正规文法和状态机关系的理解；
- 对递归下降分析、消除左递归和语法图的理解；
- 对 Bison 自动生成 parser、语法制导翻译和 AST/TAC 后端分离的理解；
- 对测试、错误恢复和可执行程序交付的工程化体会。

## 附录 A：截图清单

- [ ] Lab 1 指导书样例运行截图；
- [ ] Lab 2 语法树输出截图；
- [ ] Lab 3 指导书三地址代码输出截图；
- [ ] `make test` 全部通过截图；
- [ ] `make dist` / `java -jar ... help` 截图；
- [ ] 至少 3 项扩展功能截图；
- [ ] 可选：AST DOT 渲染图或 DOT 文本截图；
- [ ] 可选：MiniYacc/SLR ACTION/GOTO 表截图。

## 附录 B：最终提交前检查表

- [ ] 成员姓名、学号、分工填写完毕；
- [ ] 所有 `TODO` 均已处理或明确删去；
- [ ] 报告截图与 `examples/` 输出一致；
- [ ] `make test` 通过；
- [ ] `make dist` 生成 `dist/compiler-lab.jar`；
- [ ] 提交包包含实验报告、源程序、可执行程序；
- [ ] 未提交 `build/`、`.cache/` 等临时产物。
