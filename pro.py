from tkinter import ttk, messagebox
from tkinter import *
from tkcalendar import DateEntry
import sqlite3

#             PET SHOP MANAGMENT SYSTEM

# login info   username: simran
#              password: nomnom

#Available  pets
def open_pet_shop():
    
    try:
        conn = sqlite3.connect("petshop.db")
        cursor = conn.cursor()
        
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS Pets (
                PetID INTEGER PRIMARY KEY,
                Name TEXT,
                Type TEXT,
                Species TEXT,
                Price REAL,
                Age INTEGER
            )''')
        
        cursor.execute('SELECT COUNT(*) FROM Pets')
        if cursor.fetchone()[0] == 0:
            pets = [
                ('Buddy', 'Dog', 'Golden Retriever', 300, 3),
                ('Whiskers', 'Cat', 'Persian', 150, 2),
                ('Goldy', 'Fish', 'Goldfish', 20, 1),
                ('Bella', 'Dog', 'Beagle', 250, 4),
                ('Snowy', 'Cat', 'Siamese', 180, 3)
            ]
            cursor.executemany('INSERT INTO Pets (Name, Type, Species, Price, Age) VALUES (?, ?, ?, ?, ?)', pets)
            conn.commit()
    except sqlite3.Error as e:
        messagebox.showerror("Database Error", f"Error occurred: {e}")
    finally:
        if conn:
            conn.close()

    
    shop_window = Toplevel(root)
    shop_window.title("Pet Shop - Buy a Pet")
    shop_window.geometry("750x600")
    shop_window.configure(bg="#E6E6FA")

    
    cart = []

    
    def load_pets():
        try:
            conn = sqlite3.connect("petshop.db")
            cursor = conn.cursor()
            pet_table.delete(*pet_table.get_children())
            cursor.execute("SELECT * FROM Pets")
            for row in cursor.fetchall():
                pet_table.insert("", "end", values=row)
        except sqlite3.Error as e:
            messagebox.showerror("Database Error", f"Error loading pets: {e}")
        finally:
            if conn:
                conn.close()

    
    def update_total():
        total_label.config(text=f"Total Price: ${sum(float(pet[4]) for pet in cart):.2f}")

    
    def add_to_cart():
        selected = pet_table.selection()
        if not selected:
            messagebox.showwarning("Warning", "Please select a pet to add to cart!")
            return
        for item in selected:
            pet = pet_table.item(item)["values"]
            if pet not in cart:
                cart.append(pet)
                cart_table.insert("", "end", values=pet)
        update_total()

    
    def remove_from_cart():
        selected = cart_table.selection()
        if not selected:
            messagebox.showwarning("Warning", "Please select a pet to remove!")
            return
        for item in selected:
            pet = cart_table.item(item)["values"]
            cart.remove(pet)
            cart_table.delete(item)
        update_total()

    
    def checkout():
        if not cart:
            messagebox.showwarning("Warning", "Your cart is empty!")
            return
        pet_names = ", ".join(pet[1] for pet in cart)
        if not messagebox.askyesno("Confirm Purchase", f"Are you sure you want to buy {pet_names}?"):
            return
        
        bill_window = Toplevel(shop_window)
        bill_window.title("Bill Summary")
        bill_window.geometry("400x400")
        bill_window.config(bg="#E6E6FA")

        Label(bill_window, text=f"Customer: {current_username}", font=("Arial", 12), bg="#E6E6FA").pack(pady=5)

        for pet in cart:
            Label(bill_window, text=f"{pet[1]} ({pet[3]}) - ${pet[4]}", font=("Arial", 11), bg="#E6E6FA").pack()
        
        total_price = sum(float(pet[4]) for pet in cart)
        Label(bill_window, text=f"Total Price: ${total_price:.2f}", font=("Arial", 12, "bold"), fg="#008000", bg="#E6E6FA").pack(pady=10)
        Button(bill_window, text="OK", font=("Arial", 12), command=bill_window.destroy).pack(pady=10)

        
        cart.clear()
        cart_table.delete(*cart_table.get_children())
        update_total()

    #GUI Setup
    Label(shop_window, text="Available Pets", font=("Arial", 18, "bold"), fg="#4B0082", bg="#E6E6FA").pack(pady=10)
    columns = ("Pet ID", "Name", "Type", "Species", "Price", "Age")
    pet_table = ttk.Treeview(shop_window, columns=columns, show="headings", height=6)
    for col in columns:
        pet_table.heading(col, text=col)
        pet_table.column(col, width=100)
    pet_table.pack(pady=10)

    Label(shop_window, text="Your Cart", font=("Arial", 14, "bold"), fg="#4B0082", bg="#E6E6FA").pack(pady=5)
    cart_table = ttk.Treeview(shop_window, columns=columns, show="headings", height=4)
    for col in columns:
        cart_table.heading(col, text=col)
        cart_table.column(col, width=100)
    cart_table.pack(pady=5)

    total_label = Label(shop_window, text="Total Price: $0.00", font=("Arial", 14, "bold"), fg="#008000", bg="#E6E6FA")
    total_label.pack(pady=5)

    btn_frame = Frame(shop_window, bg="#E6E6FA")
    btn_frame.pack(pady=10)
    Button(btn_frame, text="Add to Cart", font=("Arial", 12), command=add_to_cart).grid(row=0, column=0, padx=5)
    Button(btn_frame, text="Remove", font=("Arial", 12), command=remove_from_cart).grid(row=0, column=1, padx=5)
    Button(btn_frame, text="Checkout", font=("Arial", 12, "bold"), bg="#4B0082", fg="white", command=checkout).grid(row=0, column=2, padx=5)

    load_pets()  

#Food and Accesories
def open_food_cart():
    try:
       
        conn = sqlite3.connect("petshop.db")
        cursor = conn.cursor()
        
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS Supplies (
                SupplyID INTEGER PRIMARY KEY,
                Name TEXT,
                Price REAL,
                Stock INTEGER
            )''')

        cursor.execute('SELECT COUNT(*) FROM Supplies')
        if cursor.fetchone()[0] == 0:
            cursor.executemany('''
                INSERT INTO Supplies (Name, Price, Stock) 
                VALUES (?, ?, ?)''', [
                ('Pet Food', 150.00, 50),
                ('Toys', 50.00, 30),
                ('Leash', 20.00, 40),
                ('Collar', 25.00, 35),
                ('Food Bowl', 10.00, 60)
            ])
            conn.commit()
    except sqlite3.Error as e:
        messagebox.showerror("Database Error", f"Error occurred: {e}")
    finally:
        conn.close()

    food_window = Toplevel(root)
    food_window.title("Food Cart")
    food_window.geometry("900x600")
    food_window.config(bg="#E6E6FA")

    def fetch_products():
        try:
            conn = sqlite3.connect("petshop.db")
            cursor = conn.cursor()
            cursor.execute("SELECT * FROM Supplies")
            rows = cursor.fetchall()
            conn.close()
            product_table.delete(*product_table.get_children())
            for row in rows:
                product_table.insert("", "end", values=row)
        except sqlite3.Error as e:
            messagebox.showerror("Database Error", f"Error fetching products: {e}")

    def add_to_bill():
        selected = product_table.selection()
        if not selected:
            messagebox.showerror("Error", "Select a product!")
            return
        item = product_table.item(selected)['values']
        name, price, stock = item[1], float(item[2]), int(item[3])
        quantity = quantity_entry.get()

        if not quantity.isdigit() or int(quantity) <= 0:
            messagebox.showerror("Error", "Enter a valid quantity!")
            return
        quantity = int(quantity)

        if quantity > stock:
            messagebox.showerror("Error", "Not enough stock!")
            return

        total_price = price * quantity
        bill_table.insert("", "end", values=(name, price, quantity, total_price))
        update_total()
        quantity_entry.delete(0, END)

    def remove_from_bill():
        selected = bill_table.selection()
        if selected:
            bill_table.delete(selected)
            update_total()
        else:
            messagebox.showerror("Error", "Select an item to remove!")

    def update_total():
        total = sum(float(bill_table.item(item)['values'][3]) for item in bill_table.get_children())
        total_label.config(text=f"Total: ₹{total:.2f}")

    def generate_bill():
        if not bill_table.get_children():
            messagebox.showwarning("Warning", "Your cart is empty!")
            return

        items = ", ".join(bill_table.item(item)["values"][0] for item in bill_table.get_children())
        if not messagebox.askyesno("Confirm Purchase", f"Are you sure you want to buy {items}?"):
            return

        bill_window = Toplevel(food_window)
        bill_window.title("Bill Summary")
        bill_window.geometry("400x400")
        bill_window.config(bg="#E6E6FA")

        
        
        Label(bill_window, text=f"Customer: {current_username}", font=("Arial", 12), bg="#E6E6FA").pack(pady=5)

        
        for item in bill_table.get_children():
            values = bill_table.item(item)["values"]
            name, price, quantity = values[0], float(values[1]), int(values[2])
            total_price = price * quantity
            Label(bill_window, text=f"{name} - ₹{price:.2f} x {quantity} = ₹{total_price:.2f}",
                  font=("Arial", 11), bg="#E6E6FA").pack()

        
        total_price = sum(float(bill_table.item(item)["values"][3]) for item in bill_table.get_children())
        Label(bill_window, text=f"Total Price: ₹{total_price:.2f}", font=("Arial", 12, "bold"), fg="#008000", bg="#E6E6FA").pack(pady=10)
        Button(bill_window, text="OK", font=("Arial", 12), command=bill_window.destroy).pack(pady=10)

        bill_table.delete(*bill_table.get_children())
        update_total()

    #GUI Setup
    Label(food_window, text="Food Cart", font=("Arial", 18, "bold"), bg="#E6E6FA", fg="#4B0082").pack(pady=10)

    
    columns = ["Supply ID", "Supply Name", "Price", "Stock Quantity"]
    product_table = ttk.Treeview(food_window, columns=columns, show="headings", height=8)
    for col in columns:
        product_table.heading(col, text=col)
        product_table.column(col, width=120)
    product_table.pack(pady=10)

    # Quantity input section
    middle_frame = Frame(food_window, bg="#E6E6FA")
    middle_frame.pack(pady=10)
    Label(middle_frame, text="Quantity:", font=("Arial", 12), bg="#E6E6FA").grid(row=0, column=0, padx=5)
    quantity_entry = Entry(middle_frame, font=("Arial", 12), width=10)
    quantity_entry.grid(row=0, column=1, padx=5)

    Button(middle_frame, text="Add", font=("Arial", 12, "bold"), bg="white", fg="black", width=10, command=add_to_bill).grid(row=0, column=2, padx=10)
    Button(middle_frame, text="Remove", font=("Arial", 12, "bold"), bg="white", fg="black", width=10, command=remove_from_bill).grid(row=0, column=3, padx=10)

    # Bill table to show selected items
    bill_frame = Frame(food_window)
    bill_frame.pack(pady=10)
    bill_columns = ["Name", "Price", "Quantity", "Total Price"]
    bill_table = ttk.Treeview(bill_frame, columns=bill_columns, show="headings", height=8)
    for col in bill_columns:
        bill_table.heading(col, text=col)
        bill_table.column(col, width=120)
    bill_table.pack()

    # Total and proceed section
    total_frame = Frame(food_window, bg="#E6E6FA")
    total_frame.pack(pady=10)
    total_label = Label(total_frame, text="₹0.00", font=("Arial", 14, "bold"), bg="#E6E6FA", fg="green")
    total_label.pack(side=LEFT, padx=10)

    Button(total_frame, text="Proceed", font=("Arial", 12, "bold"), bg="#4B0082", fg="white", width=12, command=generate_bill).pack(side=LEFT, padx=10)

    fetch_products() 

#Vet Window
def find_vets():
   
    def connect_db():
        try:
            return sqlite3.connect("petshop.db")
        except sqlite3.Error as e:
            messagebox.showerror("Database Error", f"Error connecting to database: {e}")
            return None

    
    def create_tables():
        conn = connect_db()
        if conn:
            try:
                cursor = conn.cursor()
                cursor.execute("""
                    CREATE TABLE IF NOT EXISTS vets (
                        vet_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT,
                        clinic_name TEXT,
                        location TEXT,
                        availability TEXT
                    )""")
                cursor.execute("""
                    CREATE TABLE IF NOT EXISTS appointments (
                        appointment_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        vet_id INTEGER,
                        customer_name TEXT,
                        pet_name TEXT,
                        appointment_date TEXT,
                        appointment_time TEXT,
                        status TEXT,
                        FOREIGN KEY(vet_id) REFERENCES vets(vet_id)
                    )""")
                conn.commit()
            except sqlite3.Error as e:
                messagebox.showerror("Database Error", f"Error creating tables: {e}")
            finally:
                conn.close()

    # Insert initial vet data if no data exists
    def insert_initial_vets():
        conn = connect_db()
        if conn:
            try:
                cursor = conn.cursor()
                cursor.execute("SELECT COUNT(*) FROM vets")
                if cursor.fetchone()[0] == 0:
                    cursor.executemany("""
                        INSERT INTO vets (name, clinic_name, location, availability)
                        VALUES (?, ?, ?, ?)""", [
                        ("Dr. John", "Happy Pets Clinic", "Downtown", "Mon-Fri: 9am - 6pm"),
                        ("Dr. Alice", "City Vet Care", "Uptown", "Mon-Sat: 10am - 5pm"),
                        ("Dr. Mark", "Paws Clinic", "Suburbs", "Mon-Fri: 8am - 4pm")])
                    conn.commit()
            except sqlite3.Error as e:
                messagebox.showerror("Database Error", f"Error inserting vets: {e}")
            finally:
                conn.close()

    def fetch_vets():
        conn = connect_db()
        if conn:
            try:
                cursor = conn.cursor()
                cursor.execute("SELECT vet_id, name, clinic_name, location, availability FROM vets")
                rows = cursor.fetchall()
                vet_table.delete(*vet_table.get_children())
                for row in rows:
                    vet_table.insert("", END, values=row)
            except sqlite3.Error as e:
                messagebox.showerror("Database Error", f"Error fetching vets: {e}")
            finally:
                conn.close()

    def book_appointment():
        selected = vet_table.selection()
        if not selected:
            messagebox.showwarning("Warning", "Please select a vet!")
            return

        vet_id = vet_table.item(selected[0])['values'][0]
        customer_name = name_entry.get()
        pet_name = pet_name_entry.get()
        date = date_entry.get()
        time = time_slot.get()

        if not customer_name or not pet_name or not date or not time:
            messagebox.showerror("Error", "All fields are required!")
            return

        conn = connect_db()
        if conn:
            try:
                cursor = conn.cursor()
                cursor.execute("""
                    INSERT INTO appointments (vet_id, customer_name, pet_name, appointment_date, appointment_time, status)
                    VALUES (?, ?, ?, ?, ?, ?)""", (vet_id, customer_name, pet_name, date, time, "Pending"))
                conn.commit()
                messagebox.showinfo("Success", f"Appointment confirmed with {vet_table.item(selected[0])['values'][1]} at {time} on {date}!")
            except sqlite3.Error as e:
                messagebox.showerror("Database Error", f"Error booking appointment: {e}")
            finally:
                conn.close()

        name_entry.delete(0, END)
        pet_name_entry.delete(0, END)

    def initialize_app():
        create_tables()
        insert_initial_vets()

    # Main GUI setup
    r = Tk()
    r.title("Find a Vet Clinic")
    r.geometry("900x600")
    r.configure(bg="#E6E6FA")

    Label(r, text="Available Vet Clinics", font=("Arial", 18, "bold"), bg="#E6E6FA", fg="#4B0082").pack(pady=10)

    frame = Frame(r)
    frame.pack()
    columns = ("Vet ID", "Vet Name", "Clinic Name", "Location", "Availability")
    global vet_table
    vet_table = ttk.Treeview(frame, columns=columns, show="headings", height=8)
    for col in columns:
        vet_table.heading(col, text=col)
        vet_table.column(col, width=170)
    vet_table.pack()

    # Form frame for user input
    form_frame = Frame(r, bg="#E6E6FA")
    form_frame.pack(pady=10)

    # Form fields for customer name, pet name, date, and time slot
    Label(form_frame, text="Your Name:", bg="#E6E6FA", font=("Arial", 12)).grid(row=0, column=0, padx=5, pady=5, sticky=E)
    global name_entry
    name_entry = Entry(form_frame, font=("Arial", 12))
    name_entry.grid(row=0, column=1, padx=5, pady=5)

    Label(form_frame, text="Pet Name:", bg="#E6E6FA", font=("Arial", 12)).grid(row=1, column=0, padx=5, pady=5, sticky=E)
    global pet_name_entry
    pet_name_entry = Entry(form_frame, font=("Arial", 12))
    pet_name_entry.grid(row=1, column=1, padx=5, pady=5)

    Label(form_frame, text="Select Date:", bg="#E6E6FA", font=("Arial", 12)).grid(row=2, column=0, padx=5, pady=5, sticky=E)
    global date_entry
    date_entry = DateEntry(form_frame, width=15, background="orange", foreground="white", borderwidth=2, date_pattern="yyyy-mm-dd")
    date_entry.grid(row=2, column=1, padx=5, pady=5)

    Label(form_frame, text="Time Slot:", bg="#E6E6FA", font=("Arial", 12)).grid(row=2, column=2, padx=5, pady=5, sticky=E)
    global time_slot
    time_slot = ttk.Combobox(form_frame, font=("Arial", 12), values=["10:00", "11:00", "12:00", "1:00", "3:00", "4:00"])
    time_slot.grid(row=2, column=3, padx=5, pady=5)
    time_slot.current(0)

    Button(r, text="Book Appointment", font=("Arial", 12, "bold"), bg="#4B0082", fg="white", command=book_appointment).pack(pady=10)

    initialize_app()
    fetch_vets()

#Symptom window
def open_symptom_checker():
    symptom_checker_window = Toplevel(root)
    symptom_checker_window.title("Pet Wellness Tips")
    symptom_checker_window.geometry("1000x600")
    symptom_checker_window.configure(bg="#E6E6FA")

    selected_problems = []
    selected_text = StringVar()
    result_text = StringVar()

    problems = [
        "Scratching Excessively", "Vomiting Frequently", "Limping or Difficulty Moving",
        "Loss of Appetite", "Coughing or Sneezing", "Excessive Sleeping",
        "Aggressive Behavior", "Drinking More Water Than Usual", "Weight Loss or Gain"
    ]

    
    def add_problem(problem):
        if problem not in selected_problems:
            selected_problems.append(problem)
            selected_text.set("\n".join(selected_problems))

    
    def get_recommendations():
        tips = {
            "Scratching Excessively": "Check for fleas or allergies.",
            "Vomiting Frequently": "Ensure proper diet. Consult a vet if persistent.",
            "Limping or Difficulty Moving": "Limit activity. Vet check recommended.",
            "Loss of Appetite": "Monitor for illness. Try favorite foods.",
            "Coughing or Sneezing": "Check for infections. Keep the area clean.",
            "Excessive Sleeping": "Might indicate illness. Ensure playtime.",
            "Aggressive Behavior": "Rule out pain or fear. Try training.",
            "Drinking More Water Than Usual": "Could be diabetes. Vet check advised.",
            "Weight Loss or Gain": "Monitor diet. Check for health issues."
        }
        result_text.set("\n".join(tips.get(p, "No recommendation available.") for p in selected_problems))

    
    def clear_selection():
        selected_problems.clear()
        selected_text.set("")
        result_text.set("")

    
    main_frame = Frame(symptom_checker_window, bg="#E6E6FA")
    main_frame.pack(padx=20, pady=20, fill=BOTH, expand=True)

    Label(main_frame, text="Pet Wellness Tips", font=("Arial", 18, "bold"), bg="#E6E6FA", fg="#4B0082").grid(row=0, column=1, pady=10)

    
    left_frame = Frame(main_frame, bg="#E6E6FA")
    left_frame.grid(row=1, column=0, padx=10, pady=10, sticky=N)
    Label(left_frame, text="Select Problems:", font=("Arial", 14, "bold"), bg="#E6E6FA", fg="#4B0082").pack(pady=5)

    for problem in problems:
        Button(left_frame, text=problem, command=lambda p=problem: add_problem(p), width=30, font=("Arial", 12), bg="#D8BFD8").pack(pady=5)

    
    center_frame = Frame(main_frame, bg="#E6E6FA")
    center_frame.grid(row=1, column=1, padx=20, pady=10, sticky=N)
    Label(center_frame, text="Selected Problems:", font=("Arial", 14, "bold"), bg="#E6E6FA", fg="#4B0082").pack(pady=5)
    Label(center_frame, textvariable=selected_text, bg="white", width=40, height=12, font=("Arial", 12), relief=GROOVE).pack(pady=5)

   
    button_frame = Frame(center_frame, bg="#E6E6FA")
    button_frame.pack(pady=10)
    Button(button_frame, text="Proceed", command=get_recommendations, bg="#4B0082", fg="white", font=("Arial", 12, "bold"), width=15).grid(row=0, column=0, padx=10)
    Button(button_frame, text="Clear", command=clear_selection, bg="#4B0082", fg="white", font=("Arial", 12, "bold"), width=15).grid(row=0, column=1, padx=10)

    
    right_frame = Frame(main_frame, bg="#E6E6FA")
    right_frame.grid(row=1, column=2, padx=10, pady=10, sticky=N)
    Label(right_frame, text="Recommendations:", font=("Arial", 14, "bold"), bg="#E6E6FA", fg="#4B0082").pack(pady=5)
    Label(right_frame, textvariable=result_text, wraplength=350, fg="#4B0082", bg="white", width=40, height=12, font=("Arial", 12), relief=GROOVE).pack(pady=5)

#main window
def open_main_window():
    main_window = Toplevel(root)
    main_window.title("Main Window")
    main_window.geometry("600x400")
    main_window.config(bg="#E6E6FA")

    
    Label(main_window, text="PAW CHOICE", font=('Arial', 20, 'bold'), bg="#E6E6FA", fg="#4B0082").pack(pady=20)

    frame = Frame(main_window, bg="#E6E6FA")
    frame.pack(pady=50)

    Button(frame, text="Add Pet", font=('Arial', 16, 'bold'), bg='white',fg="#4B0082", width=15, command=open_pet_shop).grid(row=0, column=0, padx=15, pady=10)
    Button(frame, text="Food Cart", font=('Arial', 16, 'bold'), bg='white',fg="#4B0082", width=15, command=open_food_cart).grid(row=0, column=1, padx=15, pady=10)
    Button(frame, text="Find Vet", font=('Arial', 16, 'bold'), bg='white',fg="#4B0082", width=15, command=find_vets).grid(row=1, column=0, padx=15, pady=10)
    Button(frame, text="Symptom Checker", font=('Arial', 16, 'bold'), bg='white',fg="#4B0082", width=15, command=open_symptom_checker).grid(row=1, column=1, padx=15, pady=10)

current_username = None

def login():
    global current_username  
    
    default_username = "simran"
    default_password = "nomnom"
    
    user = username_entry.get()
    pwd = password_entry.get()

    if user == default_username and pwd == default_password:
        messagebox.showinfo("Login", "Login Successful!")
        
        current_username = user  
        root.withdraw() 
        open_main_window()  
    else:
        messagebox.showerror("Login", "Invalid username or password!")

#login window
root = Tk()
root.title("Login")
root.geometry("500x400")
root.config(bg="#E6E6FA")
Label(root, text="Login", bg="#E6E6FA", font=("Arial", 18, "bold"),fg="#4B0082").pack(pady=10)

Label(root, text="Username", bg="#E6E6FA", font=("Arial", 12, "bold")).pack(pady=10)
username_entry = Entry(root, font=("Arial", 12))
username_entry.pack(pady=5)

Label(root, text="Password", bg="#E6E6FA", font=("Arial", 12, "bold")).pack(pady=10)
password_entry = Entry(root, show="*", font=("Arial", 12))
password_entry.pack(pady=5)
Button(root, text="Login", command=login, font=("Arial", 12, "bold"), bg="#D8BFD8", width=10).pack(pady=20)

root.mainloop()
