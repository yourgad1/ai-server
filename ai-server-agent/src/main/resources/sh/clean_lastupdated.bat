@echo off
setlocal enabledelayedexpansion

rem 定义要清理的根目录
set "ROOT_DIR=E:\file"

rem 检查目录是否存在
if not exist "%ROOT_DIR%" (
    echo 目录 %ROOT_DIR% 不存在
    pause
    exit /b 1
)

echo 开始清理 %ROOT_DIR% 下所有 .lastUpdated 文件...
echo.

rem 使用for /r递归查找并删除所有.lastUpdated文件
for /r "%ROOT_DIR%" %%f in (*.lastUpdated) do (
    if exist "%%f" (
        echo 删除: %%f
        del /f /q "%%f"
    )
)

echo.
echo 清理完成！
pause
