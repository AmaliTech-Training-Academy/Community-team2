import { readdir, readFile, writeFile } from "node:fs/promises";
import path from "node:path";

const coverageDir = path.resolve("coverage");
const baseCssPath = path.join(coverageDir, "base.css");
const classBlockStart = "/* coverage-width-classes:start */";
const classBlockEnd = "/* coverage-width-classes:end */";

async function collectIndexFiles(dir) {
  const entries = await readdir(dir, { withFileTypes: true });
  const files = [];

  for (const entry of entries) {
    const entryPath = path.join(dir, entry.name);

    if (entry.isDirectory()) {
      files.push(...(await collectIndexFiles(entryPath)));
      continue;
    }

    if (entry.isFile() && entry.name === "index.html") {
      files.push(entryPath);
    }
  }

  return files;
}

function injectWidthClasses(baseCss, widths) {
  const generatedBlock = [
    classBlockStart,
    ...widths.flatMap((width) => [
      `.w-${width} { width: ${width}%; }`,
      `.w-${100 - width} { width: ${100 - width}%; }`,
    ]),
    classBlockEnd,
  ];

  const uniqueLines = [...new Set(generatedBlock)];
  const blockText = `${uniqueLines.join("\n")}\n`;
  const blockPattern = new RegExp(
    `${classBlockStart}[\\s\\S]*?${classBlockEnd}\\n?`,
    "m",
  );

  if (blockPattern.test(baseCss)) {
    return baseCss.replace(blockPattern, blockText);
  }

  return `${baseCss.trimEnd()}\n\n${blockText}`;
}

function replaceInlineWidths(html, seenWidths) {
  return html.replace(
    /class="([^"]*\bcover-(?:fill|empty)\b[^"]*)" style="width: (\d+)%"/g,
    (_, classes, width) => {
      const numericWidth = Number(width);
      seenWidths.add(numericWidth);
      return `class="${classes} w-${numericWidth}"`;
    },
  );
}

async function main() {
  const indexFiles = await collectIndexFiles(coverageDir);
  const seenWidths = new Set();

  await Promise.all(
    indexFiles.map(async (filePath) => {
      const html = await readFile(filePath, "utf8");
      const updatedHtml = replaceInlineWidths(html, seenWidths);

      if (updatedHtml !== html) {
        await writeFile(filePath, updatedHtml, "utf8");
      }
    }),
  );

  const sortedWidths = [...seenWidths].sort((left, right) => left - right);

  if (sortedWidths.length === 0) {
    return;
  }

  const baseCss = await readFile(baseCssPath, "utf8");
  const updatedBaseCss = injectWidthClasses(baseCss, sortedWidths);

  if (updatedBaseCss !== baseCss) {
    await writeFile(baseCssPath, updatedBaseCss, "utf8");
  }
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
