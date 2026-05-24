# 实验二/三扩展执行计划

Purpose: define the single authoritative, ready-to-execute plan for extending
the Bison-based lab 2/3 implementation and preparing the final report and
presentation.

## 1. 执行定位

当前主线保持不变：

```text
Lexer -> Bison Parser -> AST -> TacEmitter -> CodeGenerator -> TAC
```

本阶段不重写整体架构，不回退到组员已有的实验二递归下降 parser。实验二/三主路径继续由 Bison parser 承担，核心交付是实验三三地址代码生成。语法树输出不是本阶段目标；如需展示 parser 中间结果，使用 AST 文本展示。

扩展原则：

- 优先做能稳定运行、能测试、能在报告里讲清楚的扩展。
- 优先补实验指导书明确提到的扩展：更多语言现象、错误定位、续编译。
- 保持默认 `--tac` 输出稳定，不让展示功能或优化功能破坏已有 fixture。
- “自己做 YACC”不进入主线；若时间充足，只作为独立原理展示。

## 2. 当前基线

已完成能力：

| 类别 | 当前能力 |
| --- | --- |
| Parser | GNU Bison 生成 Java parser |
| 架构 | Bison parser 构建 AST，TAC 生成独立实现 |
| 语句 | 赋值、`if/then`、`if/then/else`、`while/do` |
| 表达式 | 四则运算、括号、优先级、八/十/十六进制整数 |
| 扩展关系运算 | `> < = >= <= <>` |
| 复合语句 | `begin ... end` |
| 控制流 | 嵌套 `if/else`、嵌套 `while` |
| AST 展示 | `Experiment2 --ast` 输出 Bison 路径 AST，`--ast-dot` 输出 Graphviz DOT |
| TAC 优化 | `Experiment2 --tac-opt` 输出常量折叠后的 TAC |
| MiniYacc 展示 | `MiniSlrDemo` 输出固定表达式文法的 LR(0) item 集、GOTO 转移和 ACTION/GOTO 表 |
| 构建 | `make build` 自动生成 Bison parser 并编译 |
| 测试 | 指导书样例、表达式优先级、嵌套控制流、扩展关系运算、复合语句、dangling else、错误恢复、AST 展示、AST DOT 展示、常量折叠、SLR 表展示 |

当前缺口：

| 缺口 | 影响 |
| --- | --- |
| 通用自制 YACC 未落地 | 已通过固定文法 MiniYacc/SLR 展示覆盖原理说明；不替换 Bison 主线 |

## 3. 执行顺序

### A. 扩展 fixture 固化

Status: done.

目标：先不新增复杂语法，把已经实现的扩展能力固化成可测试、可演示的样例。

新增测试：

- `tests/lab3_tac_relop_extended.*`
- `tests/lab3_tac_compound.*`
- `tests/lab3_tac_dangling_else.*`

样例 1：全部关系运算。

```text
if a >= b then x = 1;
if a <= b then y = 2;
if a <> b then z = 3;
```

验证点：

- `>= <= <>` 正确进入条件跳转 TAC；
- 顶层多语句的 continuation label 正确。

样例 2：复合语句。

```text
while a < b do begin
  x = x + 1;
  y = y * 2;
end;
```

验证点：

- `while` body 支持多语句块；
- 块内语句按顺序生成 TAC；
- 循环入口、真出口、假出口和回跳正确。

样例 3：dangling else。

```text
if a > 0 then if b > 0 then x = 1 else x = 2;
```

验证点：

- `else` 绑定最近的未匹配 `if`；
- 报告中可解释 `%prec THEN`、`%nonassoc ELSE` 的作用。

完成标准：

- 新 fixture 已加入 `scripts/run_tests.sh`。
- `make test` 已全部通过。
- 设计报告已增加“扩展样例验证”。

### B. Bison 语句级错误恢复

Status: done.

目标：覆盖实验指导书扩展要求中的错误定位、续编译和简单错误处理。

实现范围：

- 只做语句级恢复。
- 错误同步点限定为 `;`、`end`、EOF。
- 错误语句不生成 TAC。
- 后续合法语句继续生成 TAC。

示例输入：

```text
x = a + ;
y = b * c;
```

期望行为：

```text
报告第 1 行表达式错误
跳过错误语句 x = a + ;
继续生成 y = b * c 的 TAC
```

建议实现：

- 在 `TacBisonParser.y` 中加入受控的 `error` 产生式。
- 在 AST 中增加 `TacError` 或 `TacEmpty` 语句节点。
- `TacEmitter` 跳过错误节点。
- `BisonTacParser` 收集错误消息，避免第一次错误就终止。
- 统一错误输出策略，保证 fixture 稳定。

新增测试：

- `tests/lab3_tac_error_recovery.in`
- `tests/lab3_tac_error_recovery.expected`

完成标准：

- 错误样例已能报告位置。
- 后续合法语句 TAC 正常输出。
- `make test` 已全部通过。
- 设计报告已增加“错误恢复设计与效果”。

### C. AST 展示模式

Status: done.

目标：让 `Parser -> AST -> TAC` 架构在汇报中直观看见。

新增入口：

```sh
java -cp build/classes Experiment2 --ast < input.in
java -cp build/classes Experiment2 --ast-dot < input.in
```

输出示例：

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

建议实现：

- 新增 `TacAstPrinter.java`。
- 新增 `TacAstDotPrinter.java`。
- 抽出 Bison parse-to-AST 入口，供 `--tac` 和 `--ast` 共用。
- `--tac` 默认行为保持不变。

新增测试：

- `tests/lab3_ast_sample.in`
- `tests/lab3_ast_sample.expected`
- `tests/lab3_ast_dot_sample.in`
- `tests/lab3_ast_dot_sample.expected`

完成标准：

- AST 输出结构稳定。
- AST DOT 输出结构稳定。
- TAC fixture 不受影响。
- PPT 可放“源程序 / AST / DOT 图 / TAC”对照。

### D. 常量折叠优化模式

Status: done.

目标：展示 TAC 生成后已经进入 IR 层，可以进行基础优化。

新增入口：

```sh
java -cp build/classes Experiment2 --tac-opt < input.in
```

优化范围：

- 只处理两个操作数都是整数常量的 `TacBinary`。
- 支持 `+ - * /`。
- 不做变量传播、公共子表达式删除、死代码删除。
- 默认 `--tac` 不启用优化，避免破坏已有输出。

示例：

```text
x = 2 + 3 * 4;
```

优化输出：

```text
x = 14
```

建议实现：

- 新增 `TacOptimizer.java` 或 `TacAstOptimizer.java`。
- 在 AST 层折叠常量表达式，再交给 `TacEmitter`。

新增测试：

- `tests/lab3_tac_constant_folding.in`
- `tests/lab3_tac_constant_folding.expected`

完成标准：

- `--tac-opt` 输出优化结果。
- `--tac` 仍输出原始三地址代码。
- 报告增加“简单 IR 优化”作为可选扩展。

### E. MiniYacc 原理展示

Status: done as standalone demo.

目标：回应“自己做一个 YACC”扩展项，但不替换 Bison 主线。

实现取舍：

- 不接入实验三 TAC。
- 不支持完整语言。
- 只作为独立原理演示。

当前已实现范围：

```text
固定表达式文法 -> LR(0) item 集族 -> GOTO 转移 -> ACTION/GOTO 表
```

暂不实现自动输入串分析轨迹；如汇报需要，可以人工选取 `id+id*id`
结合 ACTION/GOTO 表解释移进归约过程。

报告表述：

```text
主线采用 GNU Bison 完成实验要求；MiniYacc 属于 parser generator
原理验证，不作为实验三 TAC 生成路径的一部分。
```

完成标准：

- 已有 `docs/minislr-demo.md` 解释 closure/goto/ACTION/GOTO 输出边界。
- 已有 `tests/minislr_table.*` 固化输出。
- 不影响主线测试。

## 4. 报告准备计划

最终报告在 `docs/lab2-3-design-report.md` 基础上升级。

建议结构：

1. 实验目标与要求分析。
2. 总体架构：Lexer -> Bison Parser -> AST -> TAC。
3. Bison 文法设计：语句、条件、表达式、优先级、dangling else。
4. AST 设计：语句节点、表达式节点、条件节点。
5. TAC 生成算法：赋值、if、if/else、while。
6. 扩展内容：关系运算、复合语句、嵌套控制流、错误恢复、AST 展示、可选优化、MiniYacc/SLR 展示。
7. 测试设计与结果。
8. 设计取舍：不依赖原 parser、不直接在 Bison action 里输出 TAC、Bison 主线与 MiniYacc 展示分离。
9. 总结与后续方向。

报告需要补充的关键图：

- 总体架构图。
- AST 节点关系图。
- `while` TAC 标号流图。
- Bison 构建流程图。
- 错误恢复同步点示意图。
- MiniYacc/SLR ACTION/GOTO 表截图或节选。

## 5. 汇报展示计划

建议 PPT 9 到 11 页：

1. 任务定位：Bison parser + TAC，独立于原实验二 parser。
2. 总体架构：Lexer -> Bison Parser -> AST -> TAC。
3. Bison 文法核心：statement、condition、expr。
4. AST 中间表示：为什么先建 AST 再生成 TAC。
5. TAC 生成规则：赋值、if、while。
6. 已完成扩展：关系运算、复合语句、dangling else。
7. 错误恢复或 AST 展示。
8. MiniYacc/SLR 展示：固定表达式文法的 item 集和 ACTION/GOTO 表。
9. 测试结果：fixture 列表和 `make test`。
10. 设计取舍：Bison vs 自己做 YACC，AST vs 直接语义动作。
11. 总结。

现场演示输入：

1. 指导书样例：证明基本要求。
2. 复合语句 + 扩展关系运算：证明语言扩展。
3. 错误恢复或 AST 展示：证明工程化扩展。
4. `MiniSlrDemo`：证明固定文法 SLR ACTION/GOTO 表可输出。

## 6. 风险控制

| 风险 | 控制策略 |
| --- | --- |
| Bison `error` 产生式引入冲突 | 只在语句边界恢复，小步提交 |
| AST 展示影响 TAC | 共用 parse-to-AST，保持 `--tac` fixture |
| 优化改变原输出 | 新增 `--tac-opt`，默认不优化 |
| MiniYacc 范围被误解为完整 parser generator | 报告中明确它是固定文法 SLR 原理展示 |
| 汇报内容分散 | 所有扩展都围绕 Bison + AST + TAC 主线 |

## 7. Ready-To-Execute Checklist

- [x] A1 新增 `lab3_tac_relop_extended` fixture。
- [x] A2 新增 `lab3_tac_compound` fixture。
- [x] A3 新增 `lab3_tac_dangling_else` fixture。
- [x] A4 更新 `scripts/run_tests.sh` 并通过 `make test`。
- [x] B1 设计 Bison 错误恢复产生式。
- [x] B2 新增错误语句 AST 节点。
- [x] B3 `TacEmitter` 跳过错误节点。
- [x] B4 新增错误恢复 fixture 并通过 `make test`。
- [x] C1 新增 `TacAstPrinter`。
- [x] C2 增加 `Experiment2 --ast`。
- [x] C3 新增 AST fixture 并通过 `make test`。
- [x] C4 新增 `TacAstDotPrinter` 和 `Experiment2 --ast-dot`。
- [x] C5 新增 AST DOT fixture 并通过 `make test`。
- [x] D1 可选新增常量折叠优化。
- [x] D2 可选新增 `Experiment2 --tac-opt`。
- [x] D3 可选新增优化 fixture。
- [x] E1 新增 `MiniSlrDemo` 固定文法 SLR 表展示。
- [x] E2 新增 `minislr_table` fixture。
- [x] E3 新增 MiniYacc/SLR 说明文档。
- [x] R1 更新最终实验报告。
- [ ] P1 准备汇报 PPT 大纲和演示输入。
