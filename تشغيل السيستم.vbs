' تشغيل النظام المحاسبي بدون أي نافذة + فتح المتصفح تلقائيًا
' محاسب / أحمد عبدالله
Set fso = CreateObject("Scripting.FileSystemObject")
Set shell = CreateObject("WScript.Shell")
folder = fso.GetParentFolderName(WScript.ScriptFullName)

' 1) إيقاف أي نسخة قديمة شغّالة على المنفذ 5000 (حتى لا تبقى القديمة مفتوحة)
shell.Run "powershell -NoProfile -WindowStyle Hidden -Command " & _
       """Get-NetTCPConnection -LocalPort 5000 -State Listen -ErrorAction SilentlyContinue | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue }""", 0, True
WScript.Sleep 1500

' 2) تشغيل البرنامج (EXE إن وُجد) أو الكود المصدري في الخلفية
shell.CurrentDirectory = folder
exePath = folder & "\نظام المحاسبة المتكامل.exe"
If fso.FileExists(exePath) Then
    shell.Run """" & exePath & """", 0, False
Else
    ' pythonw = بايثون بدون نافذة سوداء نهائيًا (نافذة مخفية 0)
    shell.Run "pythonw app.py", 0, False
End If

' 3) استنى لحد ما السيستم يقوم (أقصى 15 ثانية)
ready = False
For i = 1 To 30
    WScript.Sleep 500
    On Error Resume Next
    Set x2 = CreateObject("MSXML2.ServerXMLHTTP.6.0")
    x2.Open "GET", "http://localhost:5000/login", False
    x2.setTimeouts 1000, 1000, 1000, 1000
    x2.Send
    If Err.Number = 0 Then
        If x2.Status = 200 Then ready = True
    End If
    Err.Clear
    On Error GoTo 0
    If ready Then Exit For
Next

shell.Run "http://localhost:5000"
