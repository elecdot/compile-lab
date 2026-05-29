# Reports

Workspace for final report integration and report-facing source material.

This directory is reserved for the final consolidated report work. The final
integrated version is expected to combine the group-member Lab 1/Lab 2 material
with the Bison path and Lab 3 implementation report.

## Files

- [final-report.md](final-report.md): working skeleton for the final
  consolidated report.
- [member-sources/](member-sources/): group-member report and implementation
  source material, including the converted Lab 1 and Lab 2 manuals.

## Usage Notes

- Keep final integrated report drafts directly under this directory or in
  clearly named subdirectories.
- Keep source material that belongs to one contributor or implementation path
  in a dedicated subdirectory instead of making `reports/` itself carry that
  narrower meaning.
- When pulling facts into the final report, cite the relevant source directory
  and keep copied claims aligned with the implementation docs and tests.

## Final Report Requirements

```txt
实验报告要求

1. 词法分析子系统

* 词法的正规式描述
* 变换后的正规文法
* 状态图
* 词法分析程序的主要数据结构与算法

2. 语法分析子系统

* 根据选择的语法分析方法进行描述。例如，如果采用递归子程序法，建议包括改写后的产生式集合，化简后的语法图
* 语法分析子系统结构
* 语法分析子系统的主要数据结构与算法

3. 三地址代码生成器

* 语法制导定义
* 算法基本思想

4. 实验体会

>注：如果采用自动生成技术，请描述采用的技术、相应的描述程序（如YACC程序）、辅助程序，以及生成的语法分析器的总体结构。

报告大纲（重要）

1. 实验任务分工

* 明确每位同学负责的具体内容

2. 基本要求完成情况

* 实验指导书中给定的测试用例能否正确执行，给出截图证明

3. 扩展内容完成情况

* 逐条列出基本要求之外所完成的内容（不限于指导书中的附加内容）

报告阶段，重点展示成果，不详细介绍技术细节（在实验报告中详细阐述）。
```
