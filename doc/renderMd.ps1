$file = Get-ChildItem $args[0]
$html = $(ConvertFrom-Markdown -Path $file)

Write-Host "Rendered $($file.BaseName)"
$html.Html > ../rendered/$($file.BaseName).html

