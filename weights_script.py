from sklearn.linear_model import SGDClassifier
from scipy.sparse import csr_matrix
from scipy import sparse
import numpy as np
import random

input_patterns_path = ""
save_weights_path = ""
save_weights_bias_path = ""

def asCsrMatrix(line):
    data = []
    row_ind = []
    col_ind = []
    for i in range(len(line) - 5):
        data.append(int(1))
        row_ind.append(int(0))
        col_ind.append(int(line[i]))

    data.append(int(line[len(line) - 5]))
    row_ind.append(int(0))
    col_ind.append(183708)
    data.append(int(line[len(line) - 4]))
    row_ind.append(int(0))
    col_ind.append(183709)
    data.append(int(line[len(line) - 3]))
    row_ind.append(int(0))
    col_ind.append(183710)
    data.append(int(line[len(line) - 2]))
    row_ind.append(int(0))
    col_ind.append(183711)
    data.append(int(line[len(line) - 1]))
    row_ind.append(int(0))
    col_ind.append(183712)


    data = np.array(data)
    row_ind = np.array(row_ind)
    col_ind = np.array(col_ind)
    return csr_matrix((data, (row_ind, col_ind)), shape=(1, 183713))

f = open(input_patterns_path, "r")

clf = SGDClassifier(loss="log" ,early_stopping=False, n_iter_no_change=50, max_iter=50000, verbose=1, warm_start=True, tol=1e-8)

i = 0
lines = []
line = f.readline().strip()
draws = 0
while line and i < 580000:
    print(len(lines))
    tmp = line.strip().split(";")
    lines.append(line)
    line = f.readline().strip()
    i += 1


for i in range(1):
    print(i)
    X = []
    y = []
    random.shuffle(lines)
    for j in range(580000):
        line_splitted = lines[j].strip().split(";")
        X.append(asCsrMatrix(line_splitted[:-1]))
        y.append(int(line_splitted[-1]))
    clf.fit(sparse.vstack(X), y)


with open(save_weights_path, 'wt') as f:
    x = clf.coef_
    for column in x:
        for row in column:
            f.write(str(row) + " ")
        f.write("\n")


with open(save_weights_bias_path, 'wt') as f:
    x = clf.intercept_
    for row in x:
        f.write(str(row) + " ")
    f.write("\n")

