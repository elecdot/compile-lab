# 实验二/三设计报告：基于 Bison 的语法分析与三地址代码生成

## 1. 实验目标

本实验实现一个面向实验二/三要求的微型编译程序。当前实现的核心目标是：

- 不依赖组员已有的实验二递归下降 parser；
- 使用 GNU Bison 独立生成实验二/三所需的语法分析器；
- 在语法分析基础上生成实验三要求的三地址代码；
- 覆盖实验指导书中的基本要求，并在能力范围内支持部分扩展语言现象和工程化能力。

本实现不把语法树输出作为核心交付。实验指导书允许“输出语法树，或者按照最左派生顺序输出产生式序列”，但当前工作重点是通过 Bison 完成语法分析，并服务三地址代码生成。因此设计中将语法分析结果构造成 AST，再由独立的 TAC 生成器遍历 AST 输出三地址代码。

## 2. 总体设计

系统采用四段式结构：

1. 词法分析：复用已有 `Lexer` 和 `Token`，识别标识符、整数、关键字、运算符和分隔符。
2. Bison 语法分析：`TacBisonParser.y` 定义实验二/三语言文法，`make build` 调用 Bison 生成 Java parser。
3. AST 构建：Bison 规约动作不直接输出 TAC，而是构建语句、表达式、条件等 AST 节点。
4. TAC 生成：`TacEmitter` 遍历 AST，使用 `CodeGenerator` 统一管理临时变量、标号和格式化输出。

这种设计把“语法识别”和“代码生成”分离，避免把大量语义动作直接堆在 Bison 规约中，也便于后续扩展错误处理、语法结构或代码生成策略。

## 3. 文件与模块

- `src/TacBisonParser.y`：Bison 语法源文件，是实验二/三 Bison parser 的源头。
- `build/generated/src/TacBisonParser.java`：由 Bison 生成的 Java parser，不手工修改。
- `src/BisonTacParser.java`：将项目已有 `Lexer` 适配为 Bison parser 需要的 token 接口。
- `src/TacAst.java`：定义 TAC 生成前使用的 AST 节点。
- `src/TacEmitter.java`：遍历 AST 并生成三地址代码。
- `src/CodeGenerator.java`：负责临时变量、标号、跳转指令和最终输出格式。
- `src/Experiment2.java`：实验二/三统一入口，默认输出 TAC，`--tac` 显式运行实验三路径。
- `Makefile`：构建入口，自动执行 Bison 生成步骤并编译 Java 源码。

## 4. 文法设计

当前 Bison 文法覆盖实验指导书给出的核心语言：

```text
program          -> top_statements opt_semi
top_statements   -> statement
                  | top_statements ; statement
statement        -> id = expr
                  | if condition then statement
                  | if condition then statement else statement
                  | while condition do statement
                  | begin compound_statements end
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

表达式优先级通过 Bison 的优先级声明处理：

```text
%left ADD SUB
%left MUL DIV
```

`if-then-else` 的悬挂 `else` 问题通过：

```text
%nonassoc THEN
%nonassoc ELSE
```

以及 `if condition then statement %prec THEN` 解决，使 `else` 默认归属于最近的未匹配 `if`。

## 5. AST 设计

Bison 规约动作构建轻量 AST，而不是立即生成代码。

语句节点包括：

- `TacAssign`：赋值语句；
- `TacIf`：条件语句，可带或不带 `else`；
- `TacWhile`：循环语句；
- `TacCompound`：复合语句。

表达式节点包括：

- `TacValue`：标识符或常量；
- `TacBinary`：二元算术表达式。

条件节点：

- `TacCondition`：保存左表达式、关系运算符和右表达式。

AST 的作用是把 Bison parser 与 TAC 生成解耦，使 parser 只负责语法结构识别和语义值组装。

## 6. 三地址代码生成设计

`TacEmitter` 按实验指导书中的语法制导定义实现 TAC 生成。

赋值语句：

```text
id = E
```

先生成表达式代码，再输出赋值代码：

```text
t1 := a + b
x = t1
```

条件语句：

```text
if C then S1
if C then S1 else S2
```

使用 `C.true` 和 `C.false` 标号生成条件跳转：

```text
if E1 relop E2 goto Ltrue
goto Lfalse
```

循环语句：

```text
while C do S
```

生成循环入口标号、条件真出口、条件假出口，并在循环体结束后跳回入口：

```text
Lbegin: if E1 relop E2 goto Lbody
goto Lnext
Lbody: ...
goto Lbegin
```

表达式：

- 加减乘除均生成临时变量；
- 乘除优先级高于加减；
- 括号表达式按 AST 结构优先计算；
- 八进制和十六进制整数由词法分析阶段转换为十进制属性值。

## 7. 基本要求覆盖情况

实验指导书基本要求覆盖如下：

| 要求 | 覆盖情况 |
| --- | --- |
| 构造语法分析程序 | 已覆盖，使用 Bison 生成 Java parser |
| 源程序可包含多个语句 | 已覆盖，顶层 `top_statements` 支持多语句 |
| 对源程序错误做适当处理 | 基本覆盖，可报告语法错误位置和非法数字 |
| 构造三地址代码生成程序 | 已覆盖，`TacEmitter` 生成 TAC |
| 连接实验一词法分析 | 已覆盖，复用已有 `Lexer` 和 `Token` |
| 调试样例并输出正确 TAC | 已覆盖，测试 fixture 覆盖指导书样例 |

需要说明的是，当前 Bison 路径不依赖原实验二 parser。原 `Parser.java` 的递归下降实现仍保留给兼容的 `--tree` 模式，但实验三 TAC 路径由 `BisonTacParser` 接管。

## 8. 扩展要求覆盖情况

当前实现覆盖了部分实验指导书中的扩展要求：

| 扩展要求 | 覆盖情况 |
| --- | --- |
| 支持全部关系运算 | 已覆盖：`> < = >= <= <>` |
| 支持复合语句 | 已覆盖：`begin ... end` |
| 更好的错误定位 | 部分覆盖：Bison 错误包含当前 token 行列 |
| 续编译、简单纠错 | 暂未作为实验三 Bison 路径目标 |
| 自己做一个 YACC | 未覆盖，当前使用 GNU Bison |

此外，当前实现还覆盖了实验指导书未展开但对代码生成正确性有帮助的能力：

- 表达式优先级测试；
- 括号表达式测试；
- 嵌套 `while`、`if/else` 控制流测试；
- 扩展关系运算 `>= <= <>` 的 TAC 测试；
- 复合语句 `begin ... end` 作为循环体的 TAC 测试；
- dangling else 绑定最近未匹配 `if` 的 TAC 测试；
- 顶层多语句 TAC 连续生成；
- Bison 生成文件进入构建流程，不依赖手工生成。

## 9. 扩展样例验证

为使扩展内容可复现、可汇报，当前测试集中新增了三个实验三 TAC
fixture。

`lab3_tac_relop_extended.*` 验证在指导书原有 `> < =` 条件基础上扩展
`>= <= <>`。这些关系运算统一通过 `relop` 规约进入 `TacCondition`，
最终保留在条件跳转指令中。

`lab3_tac_compound.*` 验证 `begin ... end` 复合语句。循环体可以包含多
条赋值语句，TAC 生成时按块内语句顺序拼接，并在块结束后跳回循环入口。

`lab3_tac_dangling_else.*` 验证 Bison 对 dangling else 的处理。通过
`%prec THEN` 和 `%nonassoc ELSE`，`else` 绑定到最近的未匹配 `if`，
符合常见程序语言语义。

## 10. 构建与运行

构建：

```sh
make build
```

构建时会执行：

```sh
bison -o build/generated/src/TacBisonParser.java src/TacBisonParser.y
javac -d build/classes ...
```

运行实验三 TAC：

```sh
java -cp build/classes Experiment2 --tac < tests/lab3_tac_sample.in
```

`Experiment2` 无参数时默认走 TAC 输出：

```sh
java -cp build/classes Experiment2 < tests/lab3_tac_sample.in
```

测试：

```sh
make test
```

当前测试覆盖：

- 实验一词法输出；
- 共享 lexer 行为；
- 实验二兼容树输出；
- 实验三指导书样例 TAC；
- 表达式优先级 TAC；
- 嵌套控制流 TAC。
- 扩展关系运算 TAC；
- 复合语句 TAC；
- dangling else TAC。

## 11. 设计取舍

本实现有几个明确取舍：

1. Bison parser 服务实验三 TAC，不强制输出语法树。
   核心目标是完成三地址代码生成。AST 是内部结构，不作为实验二展示输出。

2. 复用已有 lexer，而不是重写词法分析。
   实验二/三要求连接实验一词法分析函数，复用 lexer 能保持 token 类型、数值属性和错误 token 行为一致。

3. Bison action 只构建 AST，不直接输出代码。
   这样可以保持语法文件相对清晰，代码生成逻辑集中在 Java 类中，便于测试和维护。

4. 保留原递归下降 parser 仅用于兼容 `--tree`。
   这不是实验三路径依赖；实验三 TAC 已由 Bison parser 独立完成。

## 12. 当前限制与后续方向

当前限制：

- Bison 实验三路径暂不做语法错误续编译；
- 暂不输出 Bison parser 的语法树或最左派生产生式序列；
- 复合语句语法要求内部语句以分号结束；
- 目前没有自行实现 LR 分析表生成器。

可选后续扩展：

- 在 Bison grammar 中增加 `error` 产生式，实现更完整的恢复式错误处理；
- 输出 Bison 规约产生式序列，增强实验二展示能力；
- 给复合语句增加更宽松的末尾分号规则；
- 增加布尔表达式组合，如 `and`、`or`、`not`；
- 增加数组、声明语句或更多赋值运算形式；
- 增加针对 Bison parser 错误路径的 fixture。

## 13. 结论

当前实现已经形成一条独立于原实验二 parser 的实验二/三主流程：

```text
Lexer -> Bison-generated parser -> AST -> TacEmitter -> CodeGenerator -> TAC
```

它覆盖实验指导书基本要求，并支持全部关系运算、复合语句、表达式优先级、嵌套控制流等扩展能力。对于本阶段核心目标“通过 Bison 完成 parser 并实现实验三地址代码生成”，当前设计和实现已经满足要求。
