" Vim indent file
" Language: Tryp

if exists("b:did_indent") 
	finish
endif

let b:did_indent = 1

setlocal indentexpr="cindent(v:lnum)"
