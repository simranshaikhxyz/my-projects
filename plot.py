from tkinter import *
import random
import matplotlib.pyplot as plt
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg

#generate random colors
def generate_random_color():
    return "#{:02x}{:02x}{:02x}".format(random.randint(0, 255), random.randint(0, 255), random.randint(0, 255))

def plot_graph():
    try:
        x_values_str = x.get().split(',')
        x_values = [val.strip() for val in x_values_str]  # Handle string values for x-axis
        y_values = list(map(float, y.get().split(',')))  # Handle numeric values for y-axis
    except ValueError:
        print("Invalid input: please enter valid numbers for y-values.")
        return
    
    plot_type = var1.get()
    marker_type = var2.get()
    line_style = var3.get()

    #line style 
    if line_style == "Solid":
        line_style = '-'
    elif line_style == "Dashed":
        line_style = '--'
    elif line_style == "Dotted":
        line_style = ':'

    # matplotlib marker symbols
    marker_dict = {
        "circle": 'o',
        "star": '*',
        "point": '.',
        "pixel": ',',
        "plus": '+',
        "square": 's',
        "diamond": 'D'
    }
    marker_type = marker_dict.get(marker_type, 'o') 
    
    # Generate random colors
    x_color = generate_random_color()
    y_color = generate_random_color()
    random_color = generate_random_color()

    fig, ax = plt.subplots(figsize=(6, 5))

    #lines style
    if plot_type == "pyplot":
        ax.plot(x_values, y_values, linestyle=line_style, marker=marker_type, color=random_color)
    #show x and y separatly
        if show_separate.get():
            ax.plot(x_values, [0] * len(x_values), linestyle=line_style, marker=marker_type, color=x_color, label='X values')
            ax.plot([0] * len(y_values), y_values, linestyle=line_style, marker=marker_type, color=y_color, label='Y values')
            ax.legend()

    elif plot_type == "scatter":
        ax.scatter(x_values, y_values, marker=marker_type, color=random_color)

        if show_separate.get():
            ax.scatter(x_values, [0] * len(x_values), marker=marker_type, color=x_color, label='X values')
            ax.scatter([0] * len(y_values), y_values, marker=marker_type, color=y_color, label='Y values')
            ax.legend()
            
    elif plot_type == "bar":
        ax.bar(x_values, y_values, color=random_color)
    
 
    ax.set_title(title.get())
    ax.set_xlabel(x_label.get())
    ax.set_ylabel(y_label.get())

    canvas = FigureCanvasTkAgg(fig, master=r)
    canvas.draw()
    canvas.get_tk_widget().grid(row=10, column=2, rowspan=14, padx=10, pady=10)

#GUI 
r = Tk()
r.title("Customized Graph")

Label(r, text="Value X", font=('Arial', 13, 'italic'), width=10).grid(row=10, column=15, padx=10, pady=10)
x = Entry(r)
x.grid(row=10, column=17, padx=10, pady=10)

Label(r, text="Value Y", font=('Arial', 13, 'italic'), width=10).grid(row=11, column=15, padx=10, pady=10)
y = Entry(r)
y.grid(row=11, column=17, padx=10, pady=10)

Label(r, text="Title of Graph", font=('Arial', 13, 'italic'), width=10).grid(row=13, column=15, padx=10, pady=10)
title = Entry(r)
title.grid(row=13, column=17, padx=10, pady=10)

Label(r, text="X- Label", font=('Arial', 13, 'italic'), width=10).grid(row=15, column=15, padx=10, pady=10)
x_label = Entry(r)
x_label.grid(row=15, column=17, padx=10, pady=10)

Label(r, text="Y-Label", font=('Arial', 13, 'italic'), width=10).grid(row=17, column=15, padx=10, pady=10)
y_label = Entry(r)
y_label.grid(row=17, column=17, padx=10, pady=10)

Label(r, text="Select Plot type", font=('Arial', 11, 'italic')).grid(row=19, column=15)
var1 = StringVar(r)
var1.set("pyplot")
dropdown1 = OptionMenu(r, var1, "pyplot", "scatter", "bar")
dropdown1.grid(row=19, column=17)

Label(r, text="Select markers type", font=('Arial', 11, 'italic')).grid(row=21, column=15, pady=10)
var2 = StringVar(r)
var2.set("circle")
dropdown2 = OptionMenu(r, var2, "circle", "star", "point", "pixel", "plus", "square", "diamond")
dropdown2.grid(row=21, column=17, pady=10)

Label(r, text="Select line style", font=('Arial', 11, 'italic')).grid(row=23, column=15)
var3 = StringVar(r)
var3.set("Solid")
dropdown3 = OptionMenu(r, var3, "Solid", "Dotted", "Dashed")
dropdown3.grid(row=23, column=17)

# Option to show separate x and y plots
show_separate = BooleanVar(r)
show_separate.set(False)
Checkbutton(r, text="Show separate x and y plot", variable=show_separate, font=('Arial', 11, 'italic')).grid(row=24, column=15, columnspan=2)

Button(r, text="Plot", font=('Arial', 13, 'italic'), width=10, command=plot_graph).grid(row=25, column=17)

r.geometry("600x500")
r.mainloop()
