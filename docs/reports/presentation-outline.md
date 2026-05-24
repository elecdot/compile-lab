# 课堂汇报 PPT 大纲（3 分钟精简版）

目标：控制在 3 分钟内完成展示。整体叙事以全组成果为主，先展示词法分析和递归下降语法分析，再说明 Bison/TAC 与扩展内容。基本要求快速掠过，但每位组员的工作都要有明确展示位置。

## 时间分配

| 部分 | 页数 | 时间 |
| --- | --- | --- |
| 任务与基本要求 | 2 页 | 35 秒 |
| 组员成果展示 | 3 页 | 70 秒 |
| 集成与扩展展示 | 3 页 | 60 秒 |
| 测试与总结 | 1 页 | 15 秒 |

总计 9 页，约 180 秒。

## 第 1 页：任务分工与整体目标

标题：编译原理实验：全组任务与成果概览

要点：

- 郑天白：词法分析子系统。
- 段晰迈：实验二递归下降语法分析。
- 高子涵：Bison parser、AST、TAC 生成与补充扩展。
- 整体目标：完成词法分析、语法分析和三地址代码生成，并在各模块上补充扩展能力。

素材：

- 无需大图，使用三列表格即可。

讲稿提示：

```text
我们组按编译前端流程分工：词法分析、实验二递归下降语法分析、实验三 Bison 与三地址代码生成。接下来按模块展示成果，再集中说明扩展内容。
```

## 第 2 页：基本要求快速验证

标题：基本要求验证

要点：

- 词法分析、递归下降语法树、三地址代码生成均有对应运行结果。
- 指导书样例和扩展 fixture 纳入自动化测试。
- 当前 `make test` 覆盖 20 个 fixture。

素材：

- `media/build-test-pass.png`
- 可在角落放 `lab1-from-teammate/media/image1.png` 缩略图。

讲稿提示：

```text
基本要求只快速验证：词法、语法树和三地址代码都有对应输出，自动化测试覆盖 20 个 fixture，说明各模块已经连通。
```

## 第 3 页：郑天白：词法分析子系统

标题：词法分析：Token 识别与状态图

要点：

- 识别标识符、关键字、十进制/八进制/十六进制整数、运算符和分隔符。
- 对非法八进制、非法十六进制进行分类。
- Token 记录行号、列号，为后续错误定位提供基础。
- 输入缓冲行为已通过截图验证。

素材：

- `lab1-from-teammate/media/image1.png`
- `lab1-from-teammate/media/image4.png`

讲稿提示：

```text
词法分析是后续语法分析和 TAC 生成的基础。这里展示的是组员实现的 Token 识别结果和状态图，扩展点包括非法数值识别、行列位置记录和输入缓冲行为验证。
```

## 第 4 页：段晰迈：递归下降语法分析

标题：实验二：递归下降 Parser 与语法图

要点：

- 根据实验二要求实现递归下降语法分析。
- 对表达式文法消除左递归，使用 `E'`、`T'` 处理加减乘除。
- 输出语法树或最左派生结构。
- 保留语法图作为分析过程说明。

素材：

- `lab2-from-teammate/media/image1.png`
- `lab2-from-teammate/media/image4.png`
- `lab2-from-teammate/media/image8.png`

讲稿提示：

```text
实验二部分采用递归下降分析。组员对表达式文法做了左递归消除，并为主要非终结符画出语法图，运行时可以输出带缩进的语法树结构。
```

## 第 5 页：实验二扩展：错误处理与语言能力

标题：实验二扩展：关系运算、复合语句与续编译

要点：

- 支持六种关系运算符。
- 支持 `begin ... end` 复合语句。
- 对非法数值和语法错误进行定位。
- 遇到部分错误后继续分析，记录多个错误。

素材：

- `lab2-from-teammate/media/image9.png`
- `lab2-from-teammate/media/image10.png`
- `lab2-from-teammate/media/image15.png`

讲稿提示：

```text
实验二不是只完成基本 parser，还加入了关系运算、复合语句和错误处理扩展。这里的截图来自组员原报告，能体现错误定位和续编译效果。
```

## 第 6 页：高子涵：Bison 主路径与 AST

要点：

- 实验三主路径不依赖原递归下降 parser。
- GNU Bison 根据 `TacBisonParser.y` 生成 Java parser。
- Parser 构建 AST，再由后端统一生成 TAC。
- AST 支持文本与 DOT 输出。

素材：

- `media/lab3-ast-dot.png`

建议页内结构：

```text
Lexer -> Bison Parser -> AST -> TacEmitter -> TAC
```

讲稿提示：

```text
实验三使用 Bison 生成 parser，和实验二递归下降路径并行保留。Bison parser 负责构造 AST，TAC 生成放到独立后端，这样语法分析和代码生成职责更清楚。
```

## 第 7 页：三地址代码生成与 Bison 错误恢复

标题：TAC 生成：控制流、错误恢复与优化

要点：

- 表达式生成临时变量。
- `if/else`、`while` 生成条件跳转和标号。
- Bison `error` 产生式实现语句级错误恢复。
- `--tac-opt` 展示常量折叠。

素材：

- `media/lab3-tac-sample.png`
- `media/lab3-error-recovery.png`
- `media/lab3-tac-opt.png`

讲稿提示：

```text
TAC 生成从 AST 遍历开始，表达式产生临时变量，控制流产生标签和跳转。扩展上加入了 Bison 路径的语句级错误恢复和常量折叠优化。
```

## 第 8 页：MiniSLR 原理展示

标题：MiniSLR：ACTION/GOTO 与自动机

要点：

- 固定表达式文法生成 LR(0) item 集。
- 输出 GOTO 转移和 ACTION/GOTO 表。
- `--dot` 输出 LR(0) 状态自动机。
- 该展示用于说明 parser generator 原理，不替换 Bison 主线。

素材：

- `media/minislr-automaton.png`

讲稿提示：

```text
MiniSLR 是原理展示：它用固定表达式文法生成项目集、GOTO 转移和 ACTION/GOTO 表。主线仍使用 Bison，MiniSLR 用来说明我们理解自动生成 parser 背后的机制。
```

## 第 9 页：总结

标题：成果与边界

要点：

- 全组完成词法分析、递归下降语法分析、Bison 语法分析和三地址代码生成。
- 组员扩展包括非法数值识别、错误定位、续编译、关系运算和复合语句。
- Bison/TAC 扩展包括 AST/DOT、语句级错误恢复、常量折叠、MiniSLR。
- 边界：错误恢复是语句级；MiniSLR 是固定文法展示，不是通用 YACC。

素材：

- `media/build-test-pass.png` 可重复作为右侧小图。

讲稿提示：

```text
最终形成的是一个从 Lexer 到 Bison Parser，再到 AST 和 TAC 的完整链路。扩展重点是错误恢复、可视化、优化和 SLR 原理展示；边界也明确，避免把展示项说成通用工具。
```

## 现场演示备选命令

时间紧张时不现场运行，只展示截图。若老师要求现场验证，优先运行：

```sh
make test
java -cp build/classes Experiment2 --tac < tests/lab3_tac_sample.in
java -cp build/classes Experiment2 --tac < tests/lab3_tac_error_multiple.in
java -cp build/classes MiniSlrDemo --dot
```
