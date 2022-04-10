" Vim syntax file
" Language: Tryp

if exists("b:current_syntax") 
	finish
endif

filetype indent on

syn keyword trypKeyword proc if else var true false nil while return for class this static extends super

syn region trypString contains=trypEscape start='"' skip="\\\"" end='"'
syn match trypEscape contained "\\(\\\|n\|t\|\")"

syn match trypTodo contained "TODO\+"
syn match trypTodo contained "FIXME\+"
syn match trypTodo contained "XXX\+"

syn region trypComment contains=trypTodo start="//" end="$"
syn region trypComment contains=trypTodo start="/\*" end="\*/"

hi def link trypComment     Comment
hi def link trypEscape      SpecialChar
hi def link trypKeyword     Keyword
hi def link trypString      String
hi def link trypTodo        Todo

let b:current_syntax = "tryp"

