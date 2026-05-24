# 实验二/三报告：基于 Bison 的语法分析与三地址代码生成

## 1. 实验目标

本实验面向“语法制导的三地址代码生成”要求，实现一个微型编译程序。当前工作的核心定位是：

- 不依赖组员已有的实验二递归下降 parser；
- 使用 GNU Bison 独立生成实验二/三主路径 parser；
- 复用实验一词法分析器；
- 通过 Bison parser 构建 AST；
- 遍历 AST 生成实验三要求的三地址代码；
- 覆盖实验指导书基本要求，并完成力所能及的扩展要求。

本实现的核心交付是实验三三地址代码生成。实验指导书中实验二要求可以输出语法树或最左派生产生式序列；当前 Bison 主路径不把语法树输出作为目标，而是提供 `--ast` 模式展示 parser 生成的 AST，用于说明语法分析结果和后续语义翻译过程。

## 2. 要求对齐

### 2.1 基本要求覆盖

| 实验指导书要求 | 当前覆盖情况 |
| --- | --- |
| 构造语法分析程序 | 已覆盖。`src/TacBisonParser.y` 由 Bison 生成 Java parser。 |
| 源程序可包含多个语句 | 已覆盖。顶层 `top_statements` 支持多条语句。 |
| 对源程序错误做适当处理 | 已覆盖基本错误定位，并扩展语句级错误恢复。 |
| 构造三地址代码生成程序 | 已覆盖。`TacEmitter` 遍历 AST 生成 TAC。 |
| 连接实验一词法分析函数 | 已覆盖。复用已有 `Lexer` 和 `Token`。 |
| 调试样例并输出正确三地址代码 | 已覆盖。指导书样例纳入 `make test`。 |

### 2.2 扩展要求覆盖

| 扩展方向 | 当前覆盖情况 |
| --- | --- |
| 支持更丰富语言现象 | 已覆盖全部关系运算 `> < = >= <= <>`，复合语句 `begin ... end`，嵌套控制流。 |
| 错误定位 | 已覆盖。错误信息包含行列位置和当前 token。 |
| 续编译 | 部分覆盖。语句级错误恢复会跳过错误语句，并继续翻译后续合法语句。 |
| 简单纠错 | 当前不做自动改写，只做语句级恢复。 |
| 中间代码优化 | 已扩展 `--tac-opt` 常量折叠模式，默认 TAC 输出保持不变。 |
| 自己做一个 YACC | 未作为主线实现。当前选择实验指导书允许的 GNU Bison 自动生成工具。 |

## 3. 总体架构

系统主流程如下：

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

各阶段职责：

1. `Lexer`：识别标识符、整数、关键字、运算符和分隔符。
2. `TacBisonParser.y`：描述实验二/三语言文法，由 Bison 生成 Java parser。
3. `BisonTacParser`：适配项目已有 lexer 与 Bison parser 接口。
4. `TacAst`：保存语法分析后的抽象语法树。
5. `TacOptimizer`：可选执行 AST 层常量折叠。
6. `TacEmitter`：根据语法制导定义遍历 AST，生成三地址代码。
7. `CodeGenerator`：管理临时变量、标号、跳转指令和最终输出格式。

选择 AST 作为中间层，是为了把语法分析和 TAC 生成分离。Bison action 只负责构造 AST，不直接输出代码；这样文法文件更清晰，TAC 生成逻辑也更容易测试和扩展。

## 4. 文件结构

| 文件 | 作用 |
| --- | --- |
| `src/TacBisonParser.y` | Bison 文法源文件。 |
| `build/generated/src/TacBisonParser.java` | Bison 生成的 Java parser，不手工修改。 |
| `src/BisonTacParser.java` | 将 `Lexer` 适配给 Bison parser，并提供 parse-to-AST 入口。 |
| `src/TacAst.java` | AST 节点定义。 |
| `src/TacAstPrinter.java` | AST 文本展示，用于报告和汇报。 |
| `src/TacAstDotPrinter.java` | AST Graphviz DOT 输出，用于生成汇报图。 |
| `src/TacEmitter.java` | AST 到三地址代码的生成器。 |
| `src/TacOptimizer.java` | AST 层常量折叠优化器。 |
| `src/CodeGenerator.java` | 临时变量、标号和 TAC 输出格式管理。 |
| `src/Experiment2.java` | 命令行入口：`--tac`、`--tac-opt`、`--ast`、`--ast-dot`、`--tree`。 |
| `Makefile` | 自动运行 Bison 并编译 Java 源码。 |

## 5. Bison 文法设计

当前 Bison 文法覆盖实验指导书的核心语言，并加入部分扩展：

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
compound_stmt    -> statement ; ...
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

表达式优先级使用 Bison 声明实现：

```text
%left ADD SUB
%left MUL DIV
```

这与指导书中 `E/T/F` 分层文法表达的优先级一致，即乘除优先于加减，括号最高。

`if-then-else` 的 dangling else 问题通过以下声明处理：

```text
%nonassoc THEN
%nonassoc ELSE
```

并在无 `else` 的产生式中使用 `%prec THEN`，使 `else` 绑定到最近的未匹配 `if`。

## 6. AST 设计

Bison 规约动作构建轻量 AST。

语句节点：

- `TacAssign`：赋值语句；
- `TacIf`：条件语句；
- `TacWhile`：循环语句；
- `TacCompound`：复合语句；
- `TacError`：错误恢复产生的错误语句。

表达式节点：

- `TacValue`：标识符或常量；
- `TacBinary`：二元运算表达式。

条件节点：

- `TacCondition`：保存左表达式、关系运算符和右表达式。

AST 展示命令：

```sh
java -cp build/classes Experiment2 --ast < tests/lab3_ast_sample.in
```

DOT 展示命令：

```sh
java -cp build/classes Experiment2 --ast-dot < tests/lab3_ast_dot_sample.in
```

`--ast-dot` 只输出 Graphviz DOT 文本，项目本身不依赖 Graphviz；需要图片时可以在外部使用 `dot -Tpng` 渲染。

示例输出：

```text
Program
  While
    Condition <
      Value a
      Value b
    Compound
      Assign x
        Binary +
          Value x
          Value 1
      Assign y
        Binary *
          Value y
          Value 2
```

该模式用于展示 Bison parser 的中间结果，不改变默认三地址代码输出。

## 7. 三地址代码生成

`TacEmitter` 根据实验指导书语法制导定义生成 TAC。

### 7.1 赋值语句

```text
S -> id = E
```

先生成表达式代码，再生成赋值：

```text
t1 := a + b
x = t1
```

### 7.2 条件语句

```text
S -> if C then S1
S -> if C then S1 else S2
```

条件表达式生成真假跳转：

```text
if E1 relop E2 goto C.true
goto C.false
```

`if/else` 会额外生成分支结束后的 `goto S.next`，保证两个分支统一汇合。

### 7.3 循环语句

```text
S -> while C do S1
```

生成循环入口、条件真出口、条件假出口和回跳：

```text
Lbegin: if E1 relop E2 goto Lbody
goto Lnext
Lbody: ...
goto Lbegin
```

### 7.4 表达式

表达式生成临时变量：

- `+ - * /` 均生成中间临时变量；
- 括号由 AST 结构保证先计算；
- 八进制和十六进制数值由 lexer 转换为十进制属性值。

## 8. 扩展实现

### 8.1 全部关系运算

指导书核心条件示例包含 `> < =`。当前扩展为：

```text
relop -> > | < | = | >= | <= | <>
```

关系运算符作为 `TacCondition.op` 保存，最终进入条件跳转指令。

测试：`lab3_tac_relop_extended.*`

### 8.2 复合语句

新增：

```text
statement -> begin compound_statements end
```

复合语句允许 `if` 或 `while` 的语句体包含多条语句。TAC 生成时按块内顺序拼接。

测试：`lab3_tac_compound.*`

### 8.3 Dangling Else

Bison 通过 `%prec THEN` 和 `%nonassoc ELSE` 处理 dangling else，使 `else` 归属于最近的未匹配 `if`。

测试：`lab3_tac_dangling_else.*`

### 8.4 语句级错误恢复

错误恢复使用 Bison `error` 产生式。

策略：

- 以语句为恢复单位；
- 分号、`end` 和 EOF 作为主要同步点；
- 错误语句构造成 `TacError`；
- `TacEmitter` 跳过 `TacError`；
- 后续合法语句继续翻译。

示例输入：

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

测试：`lab3_tac_error_recovery.*`

### 8.5 AST 展示

新增 `--ast` 模式，展示 Bison parser 构建出的 AST，支撑报告和汇报中的架构说明。

测试：`lab3_ast_sample.*`

新增 `--ast-dot` 模式，输出同一棵 AST 的 Graphviz DOT 文本，用于生成报告或 PPT 中的结构图。

测试：`lab3_ast_dot_sample.*`

### 8.6 常量折叠优化

新增 `--tac-opt` 模式，在 AST 层进行简单常量折叠，然后再生成 TAC。

优化范围：

- 只折叠左右操作数都是整数常量的 `TacBinary`；
- 支持 `+ - * /`；
- 除数为 0 时不折叠，避免改变源程序语义；
- 不做变量传播、公共子表达式删除或死代码删除；
- 默认 `--tac` 输出保持未优化，保证指导书样例和既有 fixture 稳定。

示例：

```text
x = 2 + 3 * 4;
y = (10 - 6) / 2;
z = a + 1 * 2;
```

优化输出：

```text
x = 14
L0: y = 2
L1: t1 := a + 2
z = t1
```

测试：`lab3_tac_constant_folding.*`

## 9. 构建与运行

构建：

```sh
make build
```

运行 TAC：

```sh
java -cp build/classes Experiment2 --tac < tests/lab3_tac_sample.in
```

运行常量折叠 TAC：

```sh
java -cp build/classes Experiment2 --tac-opt < tests/lab3_tac_constant_folding.in
```

运行 AST 展示：

```sh
java -cp build/classes Experiment2 --ast < tests/lab3_ast_sample.in
```

运行 AST DOT 展示：

```sh
java -cp build/classes Experiment2 --ast-dot < tests/lab3_ast_dot_sample.in
```

默认模式：

```sh
java -cp build/classes Experiment2 < tests/lab3_tac_sample.in
```

等价于 `--tac`。

测试：

```sh
make test
```

## 10. 测试结果

当前 `make test` 覆盖以下 fixture：

| Fixture | 覆盖内容 |
| --- | --- |
| `lab1_sample` | 实验一 token 输出样例 |
| `lexer_contract` | 共享 lexer 行为 |
| `lab2_tree_sample` | 原实验二兼容树输出 |
| `lab2_tree_invalid_octal` | 原实验二非法八进制错误输出 |
| `lab3_tac_sample` | 指导书实验三 TAC 样例 |
| `lab3_tac_precedence` | 表达式优先级与括号 |
| `lab3_tac_nested_control` | 嵌套 `while` 和 `if/else` |
| `lab3_tac_relop_extended` | 扩展关系运算 |
| `lab3_tac_compound` | 复合语句 |
| `lab3_tac_dangling_else` | dangling else 绑定规则 |
| `lab3_tac_error_recovery` | 语句级错误恢复 |
| `lab3_ast_sample` | Bison 路径 AST 展示 |
| `lab3_ast_dot_sample` | AST 的 Graphviz DOT 输出 |
| `lab3_tac_constant_folding` | `--tac-opt` 常量折叠 |

最近一次验证结果：

```text
make test
全部 14 个 fixture 通过
```

## 11. 设计取舍

### 11.1 不依赖原实验二 Parser

原 `Parser.java` 递归下降实现仍保留给 `--tree` 兼容模式，但实验三主路径不依赖它。实验三由 Bison parser 完成语法分析，并由 AST/TAC 后端完成翻译。

### 11.2 不直接在 Bison Action 中生成 TAC

直接在 `.y` 文件中调用 `emit(...)` 可以更短，但语法和代码生成会强耦合。当前实现使用 AST 中间层，使 Bison action 只负责构造结构，TAC 生成集中在 Java 类中。

### 11.3 使用 GNU Bison 而不是自己做 YACC

实验指导书允许使用 Yacc/Bison 等自动生成工具。自行实现 LR 分析表生成器属于更高成本扩展，当前未作为主线实现。报告和汇报中可说明：本实验重点放在“使用成熟 parser generator + 自定义 AST/TAC 后端”完成语法制导翻译。

### 11.4 错误恢复边界

当前错误恢复是语句级恢复，不做复杂自动纠错。这样能满足“定位 + 续编译”的核心展示，同时避免错误恢复逻辑影响正确程序的 TAC 生成。

## 12. 当前限制与后续方向

当前限制：

- 错误恢复只到语句级，不做复杂纠错；
- 复合语句内部目前要求语句以分号结束；
- 未实现布尔短路表达式；
- 未实现声明、类型检查、数组等语义分析内容；
- 未实现自制 LR 分析表生成器。

可选后续扩展：

- 更完整的 Bison 错误恢复；
- 布尔表达式 `and/or/not` 与短路 TAC；
- MiniYacc 原理展示：固定文法的 LR(0)/SLR item 集和 ACTION/GOTO 表。

## 13. 总结

当前实现形成了独立于原实验二 parser 的实验二/三主流程：

```text
Lexer -> Bison-generated parser -> AST -> TacEmitter -> CodeGenerator -> TAC
```

它覆盖实验指导书基本要求，并完成了全部关系运算、复合语句、dangling else、语句级错误恢复、AST 文本展示、AST DOT 展示和常量折叠等扩展。核心目标“通过 Bison 完成 parser 并实现实验三地址代码生成”已经达成。
