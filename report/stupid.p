set term postscript eps enhanced
set output "stupid.eps"
set xlabel "Parameter n"
set ylabel "Tries to Winning T(n)"
set yrange [0 : 700]
set xrange [-1 : 11]
y(x) = 1*x + 1
plot \
"stupid.data" using 1:2 title "T(n)" with points#,\
#y(x) title "x + 1"
