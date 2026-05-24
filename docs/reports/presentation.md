## 编译原理实验汇报：第二组

| 成员 | 负责内容 | 主要成果 |
| --- | --- | --- |
| 郑天白 | 词法分析子系统 | Token 识别、非法整数检测、输入处理 |
| 段晰迈 | 实验二递归下降语法分析 | 语法树输出、语法图、错误处理扩展 |
| 高子涵 | Bison 与三地址代码生成 | AST、TAC、错误恢复、优化、MiniSLR |

## 基本要求快速验证

- 词法分析、语法树输出、三地址代码生成均有对应结果。
- 指导书样例与扩展样例纳入自动化测试。
- 当前测试覆盖 20 个 fixture。

![自动化测试通过](media/build-test-pass.png){ width=8.2in }

## 词法分析子系统

- 识别标识符、关键字、三类整数、运算符和分隔符。
- 支持非法八进制、非法十六进制识别。
- Token 记录行列位置，为错误定位提供基础。

![词法状态图](lab1-from-teammate/media/image4.png){ width=7.4in }

## 递归下降语法分析

- 实验二采用递归下降分析方法。
- 表达式文法消除左递归，使用 `E'`、`T'` 处理运算优先级。
- 输出语法树或最左派生结构。

![S 的语法图](lab2-from-teammate/media/image1.png){ width=5.2in }

![E 的语法图](lab2-from-teammate/media/image4.png){ width=4.2in }

## 实验二扩展能力

- 支持六种关系运算符：`> < = >= <= <>`。
- 支持 `begin ... end` 复合语句。
- 支持错误定位与续编译。

![混合错误续编译验证](lab2-from-teammate/media/image15.png){ width=7.8in }

## Bison 主路径与 AST

- 实验三主路径使用 GNU Bison 生成 Java parser。
- Bison parser 构建 AST，再由后端生成 TAC。
- AST 支持文本输出和 Graphviz DOT 可视化。

```text
Lexer -> Bison Parser -> AST -> TacEmitter -> TAC
```

![AST DOT 图](media/lab3-ast-dot.png){ width=7.1in }

## 三地址代码生成与扩展

- 表达式生成临时变量。
- `if/else`、`while` 生成条件跳转和标号。
- Bison 路径支持语句级错误恢复和常量折叠。

![三地址代码输出](media/lab3-tac-sample.png){ width=5.0in }

![错误恢复输出](media/lab3-error-recovery.png){ width=3.6in }

## MiniSLR 原理展示

- 固定表达式文法生成 LR(0) item 集。
- 输出 GOTO 转移和 ACTION/GOTO 表。
- `--dot` 输出 LR(0) 状态自动机。
- 用于说明 parser generator 的基本原理，不替代 Bison 主线。

![MiniSLR 自动机](media/minislr-automaton.png){ width=7.6in }

## 总结

- 全组完成词法分析、递归下降语法分析、Bison 语法分析与 TAC 生成。
- 扩展内容包括非法数值识别、错误定位、续编译、复合语句、AST/DOT、常量折叠和 MiniSLR。
- 边界说明：错误恢复是语句级恢复；MiniSLR 是固定文法展示，不是通用 YACC。

![自动化测试通过](media/build-test-pass.png){ width=7.2in }
