set curpath=%cd%
for /r %curpath%\src\ %%v in (*.java) do call .\tool\delbom %%v