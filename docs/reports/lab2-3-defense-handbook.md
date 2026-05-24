# Lab2/3 答辩准备手册

> 适用范围：本手册只针对本组实验二/三主线，即“Bison 语法分析 + AST 中间层 + 三地址代码生成”部分。词法分析和组员递归下降实验二部分可参考 `docs/reports/group-report.md`、`docs/reports/lab1-from-teammate/README.md` 和 `docs/reports/lab2-from-teammate/README.md`。

## 0. 最高优先级记忆版

如果答辩时间很短，先记住下面这段：

```text
我们实验三主线没有依赖原实验二递归下降 parser，而是使用 GNU Bison 根据 TacBisonParser.y 生成 Java parser。parser 规约时只构造 AST，不直接输出三地址代码；之后 TacEmitter 按实验指导书的语法制导思想遍历 AST，生成临时变量、标号、条件跳转和 goto。这样语法分析和代码生成分离，便于扩展错误恢复、AST/DOT 展示和常量折叠。MiniSLR 是固定表达式文法下的 LR 分析表原理展示，不是完整通用 YACC。
```

最重要边界：

- `--tac` 是实验三主交付。
- `--tree` 是组员递归下降实验二兼容路径。
- `--ast`、`--ast-dot` 是展示 parser 中间结果。
- `--tac-opt` 是额外优化展示，不改变默认 TAC。
- `MiniSlrDemo` 是固定文法 SLR 原理展示，不是通用 parser generator。
- 错误恢复是语句级恢复，不是完整自动纠错。

## 1. 指导书要求与当前实现对应关系

### 1.1 基本要求

| 指导书要求 | 当前实现 | 答辩说法 |
| --- | --- | --- |
| 构造语法分析程序 | `src/TacBisonParser.y` 经 Bison 生成 Java parser | 使用指导书允许的 Bison 自动生成工具 |
| 可包含多个语句 | `top_statements` 支持多语句；`opt_semi` 允许末尾分号可省略 | 支持多语句，末尾分号可省略是输入友好扩展 |
| 错误适当处理 | Bison `error` 产生式 + `TacError` 节点 | 语句级错误恢复，错误语句不生成 TAC |
| 构造三地址代码生成程序 | `TacEmitter` 遍历 AST 输出 TAC | 按语法制导定义思想实现 |
| 连接实验一词法分析 | `BisonTacParser` 适配已有 `Lexer` | 不另写词法器，复用实验一 |
| 指导书样例正确输出 | `tests/lab3_tac_sample.*` | 已纳入 `make test` |

### 1.2 扩展要求

| 指导书扩展方向 | 当前实现 | 边界 |
| --- | --- | --- |
| 全部关系运算 | `> < = >= <= <>` | 已实现 |
| 复合语句 | `begin ... end` | 已实现，块内语句以分号结束 |
| 错误定位、续编译 | 行列位置 + 语句级恢复 | 不做复杂自动纠错 |
| 简单错误纠正 | 组员递归下降路径有相关展示；Bison TAC 路径不主打 | 不要声称 Bison 路径完整纠错 |
| 自己做 YACC | `MiniSlrDemo` 展示固定表达式文法 SLR 表和 DOT 自动机 | 不是通用 YACC，不接入 TAC |
| 中间代码优化 | `--tac-opt` 常量折叠 | 额外扩展，不是指导书基本要求 |

## 2. 主线架构

### 2.1 总流程

```text
Source
  -> Lexer
  -> Bison-generated parser
  -> AST
  -> optional TacOptimizer
  -> TacEmitter
  -> CodeGenerator
  -> TAC
```

对应文件：

| 模块 | 文件 |
| --- | --- |
| 词法器 | `src/Lexer.java`、`src/Token.java` |
| Bison 文法 | `src/TacBisonParser.y` |
| Bison 生成 parser | `build/generated/src/TacBisonParser.java` |
| 适配层 | `src/BisonTacParser.java` |
| AST 节点 | `src/TacAst.java` |
| TAC 生成 | `src/TacEmitter.java`、`src/CodeGenerator.java` |
| AST 展示 | `src/TacAstPrinter.java`、`src/TacAstDotPrinter.java` |
| 常量折叠 | `src/TacOptimizer.java` |
| 命令入口 | `src/Experiment2.java` |
| MiniSLR | `src/MiniSlrDemo.java` |

### 2.2 为什么先建 AST，再生成 TAC？

标准回答：

```text
直接在 Bison action 中生成 TAC 也可以，但会把语法规则和代码生成逻辑强耦合。我们选择先构造 AST，再由 TacEmitter 统一生成 TAC。这样 Bison 文件只负责语法结构，TAC 生成、错误节点跳过、常量折叠、AST/DOT 展示都能放在独立 Java 类里实现，更容易测试和扩展。
```

优点：

- 文法文件更清晰；
- TAC 生成逻辑集中；
- `--tac`、`--tac-opt`、`--ast`、`--ast-dot` 可以共享 parse-to-AST 结果；
- 后续优化可以在 AST 层执行；
- 错误语句可以用 `TacError` 表示，后端统一跳过。

缺点：

- 比直接 action 输出多了一层 AST；
- 对于很小的实验，代码量略多。

答辩时可说：

```text
这是一种更清楚的实现方式，不改变指导书语法制导定义的思想。
```

## 3. Bison 文法设计

### 3.1 当前核心文法

简化版：

```text
program        -> top_statements opt_semi
top_statements -> statement
                | top_statements ; statement

statement      -> id = expr
                | if condition then statement
                | if condition then statement else statement
                | while condition do statement
                | begin compound_statements end
                | error

condition      -> expr relop expr
relop          -> > | < | = | >= | <= | <>

expr           -> expr + expr
                | expr - expr
                | expr * expr
                | expr / expr
                | ( expr )
                | factor

factor         -> id | int8 | int10 | int16
```

### 3.2 与指导书文法的差异

指导书表达式文法：

```text
E -> E + T | E - T | T
T -> T * F | T / F | F
F -> ( E ) | id | int8 | int10 | int16
```

Bison 实现没有显式写 `E/T/F` 三层，而是写成：

```text
expr -> expr + expr
expr -> expr * expr
...
```

并用优先级声明：

```bison
%left ADD SUB
%left MUL DIV
```

答辩说法：

```text
在 Bison 中可以用优先级声明表达与 E/T/F 分层文法等价的运算优先级。乘除声明在加减之后，因此优先级更高；括号通过 SLP expr SRP 产生式保证最高优先级。
```

### 3.3 Dangling Else 如何处理？

问题：

```text
if a > 0 then if b > 0 then x = 1 else x = 2;
```

`else` 应该属于内层 `if`。

Bison 处理：

```bison
%nonassoc THEN
%nonassoc ELSE

IF condition THEN statement %prec THEN
IF condition THEN statement ELSE statement
```

答辩说法：

```text
无 else 的 if 产生式使用 %prec THEN，使其优先级低于 ELSE。当 Bison 遇到 else 时选择移进，从而把 else 绑定到最近的未匹配 if。这符合常见语言规则。
```

### 3.4 末尾分号为什么可省略？

当前文法有：

```bison
program -> top_statements opt_semi
opt_semi -> empty | SEMI
```

这比指导书 `L -> S ;` 稍微宽松。

标准说法：

```text
这是输入友好性扩展。实验指导书要求支持以分号分隔的多语句，我们仍然支持；同时允许最后一条语句末尾分号省略。报告中不把它作为基本要求强调。
```

如果老师追问是否严格：

```text
如果需要严格匹配指导书，只需去掉 opt_semi，让 program 必须以分号结束。当前实现选择宽松接受，不影响指导书样例。
```

## 4. AST 设计

### 4.1 语句节点

| 节点 | 含义 |
| --- | --- |
| `TacAssign` | 赋值语句 `id = expr` |
| `TacIf` | `if` / `if else` |
| `TacWhile` | `while` |
| `TacCompound` | `begin ... end` |
| `TacError` | 错误恢复后的错误语句 |

### 4.2 表达式节点

| 节点 | 含义 |
| --- | --- |
| `TacValue` | 标识符或常量 |
| `TacBinary` | 二元表达式 |
| `TacCondition` | 条件表达式，保存左右表达式和关系运算符 |

### 4.3 为什么有 `TacError`？

答辩说法：

```text
Bison 通过 error 产生式恢复后，需要在语句列表里留下一个占位，表示这是一条错误语句。TacEmitter 遍历时遇到 TacError 就跳过，不为错误语句生成 TAC。这样后续合法语句仍然可以继续翻译。
```

## 5. 三地址代码生成

### 5.1 指导书语义规则如何落到代码？

指导书写法：

```text
E.place = newtemp
E.code = E1.code || T.code || gen(E.place := E1.place + T.place)
```

当前实现：

```java
String left = emitExpr(binary.left);
String right = emitExpr(binary.right);
String temp = codeGen.newTemp();
codeGen.emit(temp + " := " + left + " + " + right);
return temp;
```

对应关系：

| 指导书属性 | 当前实现 |
| --- | --- |
| `E.place` | `emitExpr()` 的返回值 |
| `E.code` | `codeGen.emit(...)` 逐行累积 |
| `newtemp` | `codeGen.newTemp()` |
| `newlabel` | `codeGen.newLabel()` |
| `gen(...)` | `codeGen.emit(...)` |

答辩说法：

```text
虽然没有直接写 E.code、E.place 这样的属性变量，但 emitExpr 的返回值就相当于 place，CodeGenerator 保存的代码列表就相当于 code。
```

### 5.2 赋值语句

输入：

```text
x = a + b * c;
```

TAC：

```text
t1 = b * c
t2 := a + t1
x = t2
```

生成过程：

1. 先生成 `b * c`，得到 `t1`；
2. 再生成 `a + t1`，得到 `t2`；
3. 最后生成 `x = t2`。

### 5.3 条件语句

指导书规则：

```text
C.true = newlabel
C.false = S.next
C.code = if E1 relop E2 goto C.true
         goto C.false
```

当前实现：

```java
emitCondition(condition, trueLabel, nextLabel)
```

输出形态：

```text
if a > b goto L1
goto L0
L1: x = 1
L0:
```

### 5.4 if-else

输出结构：

```text
if C goto Ltrue
goto Lfalse
Ltrue: thenBranch
goto Lnext
Lfalse: elseBranch
Lnext:
```

答辩说法：

```text
if-else 需要 true、false 和 next 三类位置。then 分支执行完要跳过 else 分支，因此会额外生成 goto next。
```

### 5.5 while

输出结构：

```text
Lbegin: if C goto Lbody
goto Lnext
Lbody: body
goto Lbegin
Lnext:
```

答辩说法：

```text
while 的 begin label 是循环入口；条件为真进入 body，条件为假跳到循环之后；body 执行完回到 begin。
```

### 5.6 为什么顺序赋值不再输出 L0/L1？

这是最近修正过的点。

错误旧行为：

```text
x = 14
L0: y = 2
L1: t1 := a + 2
z = t1
```

问题：

```text
顺序执行天然进入下一条语句，不需要 label。L0/L1 只有被 goto 或条件跳转引用时才有意义。
```

当前正确行为：

```text
x = 14
y = 2
t1 := a + 2
z = t1
```

答辩说法：

```text
一开始为了统一控制流出口，给顶层语句都分配 nextLabel。后来发现纯顺序语句不需要 label，因此改成只有 if、while 或包含控制流的复合语句才创建 nextLabel，并且只有 label 被引用时才输出。
```

## 6. CodeGenerator 输出格式

### 6.1 临时变量

```text
t1, t2, t3, ...
```

由 `newTemp()` 生成。

### 6.2 标号

```text
L0, L1, L2, ...
```

由 `newLabel()` 和 `newProgramNextLabel()` 生成。

`L0` 的用途：

```text
程序顶层第一个控制流语句的 next label，通常表示控制流语句结束后的位置。
```

### 6.3 为什么有时 label 和下一条语句在同一行？

`CodeGenerator.getCodes()` 会把：

```text
L1:
t1 := a + b
```

格式化为：

```text
L1: t1 := a + b
```

原因是实验指导书样例也是类似格式：

```text
L1: t1 := a3 + 15
```

答辩说法：

```text
这是输出格式优化，不改变三地址代码语义。
```

## 7. 错误恢复

### 7.1 当前 Bison 路径做了什么？

- Bison 开启 verbose error；
- `statement -> error` 产生式捕获错误语句；
- 错误信息带行列和当前 token；
- 错误语句构造成 `TacError`；
- `TacEmitter` 跳过 `TacError`；
- 后续合法语句继续生成 TAC。

### 7.2 示例

输入：

```text
x = a + ;
y = b * c;
```

输出：

```text
语法错误 [行1列9]: syntax error, unexpected SEMI（当前token: SEMI ';'）
t1 = b * c
y = t1
```

### 7.3 覆盖了哪些错误测试？

| 测试 | 覆盖 |
| --- | --- |
| `lab3_tac_error_recovery` | 缺表达式后继续 |
| `lab3_tac_error_missing_rparen` | 缺右括号 |
| `lab3_tac_error_missing_then` | 缺 `then` |
| `lab3_tac_error_compound_recovery` | 复合语句内部错误 |
| `lab3_tac_error_multiple` | 多个错误后继续 |

### 7.4 边界

一定要说清楚：

```text
当前 Bison TAC 路径是语句级错误恢复，不是完整自动纠错器。
```

不做：

- 自动补分号；
- 自动补 `then`；
- 自动补 `end`；
- 复杂错误汇总格式；
- 词法非法字符后的复杂续编译。

如果老师问“为什么不做复杂纠错”：

```text
指导书基本要求是适当处理错误，扩展要求提到可以做定位、续编译和简单纠错。我们在 Bison TAC 路径选择了较稳定的语句级恢复，避免纠错规则影响正确程序的 TAC 生成。组员递归下降路径中有更多简单纠错展示。
```

## 8. 常量折叠

### 8.1 入口

```sh
java -cp build/classes Experiment2 --tac-opt < tests/lab3_tac_constant_folding.in
```

### 8.2 做什么？

输入：

```text
x = 2 + 3 * 4;
y = (10 - 6) / 2;
z = a + 1 * 2;
```

输出：

```text
x = 14
y = 2
t1 := a + 2
z = t1
```

解释：

- `2 + 3 * 4` 全是常量，折叠为 `14`；
- `(10 - 6) / 2` 折叠为 `2`；
- `a + 1 * 2` 只能折叠 `1 * 2`，不能折叠 `a + 2`。

### 8.3 为什么不默认启用？

答辩说法：

```text
默认 --tac 保持指导书样例风格，方便验证基本要求。优化作为 --tac-opt 单独入口展示，避免改变基础输出。
```

### 8.4 边界

只做：

- 整数字面量；
- `+ - * /`；
- 除数为 0 时不折叠。

不做：

- 变量传播；
- 公共子表达式删除；
- 死代码删除；
- 控制流优化。

## 9. AST 与 DOT 展示

### 9.1 AST 文本

命令：

```sh
java -cp build/classes Experiment2 --ast < tests/lab3_ast_sample.in
```

用途：

```text
展示 Bison parser 识别出的结构，说明 parser 和 TAC 后端之间确实有 AST 中间层。
```

### 9.2 AST DOT

命令：

```sh
java -cp build/classes Experiment2 --ast-dot < tests/lab3_ast_dot_sample.in
```

用途：

```text
生成 Graphviz DOT，用于报告或 PPT 中展示 AST 图。
```

边界：

```text
AST/DOT 是展示工具，不是指导书基本要求。
```

## 10. MiniSLR

### 10.1 它是什么？

`MiniSlrDemo` 是固定表达式文法的 SLR 原理展示。

固定文法：

```text
(0) S' -> E
(1) E -> E + T
(2) E -> T
(3) T -> T * F
(4) T -> F
(5) F -> ( E )
(6) F -> id
```

### 10.2 输出什么？

默认命令：

```sh
java -cp build/classes MiniSlrDemo
```

输出：

- 编号产生式；
- LR(0) item 集；
- GOTO 转移；
- ACTION/GOTO 表。

DOT 命令：

```sh
java -cp build/classes MiniSlrDemo --dot
```

输出：

- LR(0) 状态自动机 DOT。

### 10.3 为什么做它？

指导书扩展项提到：

```text
自己做一个 YACC：构建一个自动生成程序，生成实现 LR 分析的分析表，并利用此分析表实现语法分析。
```

我们的实现属于“部分覆盖 / 原理展示”：

```text
固定表达式文法 -> LR(0) item 集 -> GOTO -> ACTION/GOTO -> DOT
```

没有做到：

- 任意文法输入；
- 完整通用 YACC；
- 用生成表真正分析任意输入串；
- 接入实验三 TAC 主线。

标准答辩说法：

```text
MiniSLR 是为了展示 LR 分析表生成原理。实验三生产路径仍采用 GNU Bison。我们不把 MiniSLR 宣称为完整 YACC，只把它作为固定文法下的原理展示。
```

## 11. 测试体系

### 11.1 测试入口

```sh
make test
```

### 11.2 覆盖内容

| Fixture | 覆盖 |
| --- | --- |
| `lab1_sample` | 词法输出 |
| `lexer_contract` | 共享 lexer 行为 |
| `lab2_tree_sample` | 递归下降语法树 |
| `lab2_tree_invalid_octal` | 实验二错误 |
| `lab3_tac_sample` | 指导书 TAC 样例 |
| `lab3_tac_precedence` | 表达式优先级 |
| `lab3_tac_nested_control` | 嵌套控制流 |
| `lab3_tac_relop_extended` | 扩展关系运算 |
| `lab3_tac_compound` | 复合语句 |
| `lab3_tac_dangling_else` | dangling else |
| `lab3_tac_error_*` | 错误恢复 |
| `lab3_ast_sample` | AST 文本 |
| `lab3_ast_dot_sample` | AST DOT |
| `lab3_tac_constant_folding` | 常量折叠 |
| `minislr_table` | SLR 表 |
| `minislr_dot` | SLR 自动机 DOT |

当前一共 20 个 fixture。

## 12. 常见答辩问题与推荐回答

### Q1：你们实验三是否依赖原实验二递归下降 parser？

答：

```text
不依赖。原 Parser.java 保留给 --tree 模式输出实验二语法树；实验三主路径使用 Bison 生成的 TacBisonParser，解析后构造 AST，再生成 TAC。
```

### Q2：为什么不用 Bison action 直接生成 TAC？

答：

```text
可以直接生成，但会把语法和代码生成耦合在 .y 文件里。我们先建 AST，再由 TacEmitter 生成 TAC，便于展示 AST、做常量折叠和统一处理错误节点。
```

### Q3：这还是语法制导翻译吗？

答：

```text
是。指导书中的 E.place、E.code、S.next 等属性，在实现中分别对应 emitExpr 的返回值、CodeGenerator 的代码列表和 nextLabel。只是实现形式从“在产生式动作中拼接属性”改成“AST 遍历时计算属性”。
```

### Q4：为什么表达式文法没有写 E/T/F？

答：

```text
Bison 支持用优先级声明解决表达式优先级。%left ADD SUB 和 %left MUL DIV 等价表达了乘除高于加减，括号产生式保证括号最高优先级。
```

### Q5：if-else 二义性怎么处理？

答：

```text
通过 %nonassoc THEN、%nonassoc ELSE 和 %prec THEN，使 else 移进并绑定最近的未匹配 if。
```

### Q6：错误恢复做到什么程度？

答：

```text
Bison TAC 路径做到语句级错误恢复：报告行列位置和当前 token，错误语句不生成 TAC，后续合法语句继续翻译。它不是完整自动纠错器。
```

### Q7：为什么常量折叠不是默认？

答：

```text
默认 --tac 保持指导书样例风格，便于验证基本要求。--tac-opt 是额外优化入口，用来展示 AST 层可以做简单优化。
```

### Q8：MiniSLR 是完整 YACC 吗？

答：

```text
不是。它是固定表达式文法下的 LR item 集、GOTO、ACTION/GOTO 表和 DOT 自动机展示。完整通用 YACC 需要支持任意文法输入和用生成表分析输入串，这不是当前主线目标。
```

### Q9：为什么顺序赋值没有 label？

答：

```text
顺序执行天然进入下一条语句，不需要 label。label 只有在被 goto 或条件跳转引用时才输出。这样生成的 TAC 更干净，也更符合三地址代码习惯。
```

### Q10：支持声明、类型检查、数组吗？

答：

```text
不支持。指导书当前文法没有声明、类型、数组等语义分析内容，我们没有扩展到这些方向。
```

### Q11：支持布尔短路吗？

答：

```text
不支持。当前条件只支持 E relop E。布尔 and/or/not 和短路翻译可以作为后续扩展。
```

### Q12：为什么末尾分号可省略？

答：

```text
这是输入友好性扩展。指导书样例和分号分隔仍然支持。如果要求严格按照 L -> S ;，去掉 opt_semi 即可。
```

### Q13：十六进制、八进制如何变成十进制？

答：

```text
Lexer 在 token 属性值中已经把八进制和十六进制转换为十进制字符串。TacEmitter 直接使用 token.value，所以 TAC 中出现的是十进制数值。
```

### Q14：为什么有时用 `=`，有时用 `:=`？

答：

```text
指导书语义规则中用 :=，样例输出中也混用了 =。当前输出整体保持指导书样例风格：临时变量和赋值均表达三地址代码赋值语义，不影响语义理解。
```

如果被追问，可以说：

```text
这属于输出格式问题，不是语义问题；如果需要完全统一，可以把 CodeGenerator/TacEmitter 中的赋值格式统一成 :=。
```

## 13. 现场演示命令

构建：

```sh
make build
```

测试：

```sh
make test
```

指导书样例 TAC：

```sh
java -cp build/classes Experiment2 --tac < tests/lab3_tac_sample.in
```

错误恢复：

```sh
java -cp build/classes Experiment2 --tac < tests/lab3_tac_error_multiple.in
```

常量折叠：

```sh
java -cp build/classes Experiment2 --tac-opt < tests/lab3_tac_constant_folding.in
```

AST：

```sh
java -cp build/classes Experiment2 --ast < tests/lab3_ast_sample.in
```

MiniSLR：

```sh
java -cp build/classes MiniSlrDemo
java -cp build/classes MiniSlrDemo --dot
```

## 14. 不要踩的坑

### 14.1 不要说“我们完整实现了 YACC”

正确说法：

```text
我们实现了固定表达式文法下的 MiniSLR 分析表和自动机展示。
```

### 14.2 不要说“我们完整实现了自动纠错”

正确说法：

```text
Bison TAC 路径实现了语句级错误定位与续编译。
```

### 14.3 不要说“AST 是指导书基本要求”

正确说法：

```text
AST 是我们为解耦语法分析和 TAC 生成引入的中间表示，也用于报告展示。
```

### 14.4 不要把 `--tac-opt` 当成默认行为

正确说法：

```text
默认 `--tac` 输出未优化 TAC；`--tac-opt` 是优化扩展。
```

### 14.5 不要说“严格完全等同指导书文法”

原因：

- 末尾分号可省略；
- 支持 `>= <= <>`；
- 支持 `begin ... end`；
- 有 AST/DOT 和优化等扩展。

正确说法：

```text
基本语言覆盖指导书文法，同时加入了若干扩展。
```

## 15. 一页纸速背

```text
实验三主线：
Lexer -> Bison Parser -> AST -> TacEmitter -> CodeGenerator -> TAC

Bison 文件：
src/TacBisonParser.y

AST 节点：
TacAssign / TacIf / TacWhile / TacCompound / TacError
TacValue / TacBinary / TacCondition

TAC 关键：
emitExpr 返回 place
CodeGenerator 保存 code
newTemp 生成 t1/t2
newLabel 生成 L0/L1

控制流：
if：条件真跳 then，假跳 next
if-else：then 后 goto next，false label 进入 else
while：begin -> condition -> body -> begin

扩展：
>= <= <>
begin ... end
dangling else
语句级错误恢复
AST / AST DOT
常量折叠
MiniSLR 表和 DOT

边界：
MiniSLR 不是完整 YACC
错误恢复不是完整自动纠错
--tac-opt 不是默认输出
```

