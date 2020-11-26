import sys
import numpy as np


DEBUG = 0
num_procs = -1
num_msgs = -1

# dependency[p][q] = boolean --> tells whether process p depends on process q
dependency = list()

# past[p][m] = [(q1, m1), (q2, m2), ...] --> the past of a message m sent by p is the list of all 
# messages (from processes on which p depends) delivered before the broadcast of m
past = list()

def parse_args():
    global num_procs
    global num_msgs
    global dependency


    if len(sys.argv) != 3:
        print("Usage:", sys.argv[0], "hosts config")
        exit(1)
    
    hosts = sys.argv[1]
    config = sys.argv[2]

    # read number of processes
    with open(hosts) as file:
        num_procs = len(file.readlines())


    # read number of messages
    with open(config) as file:
        config_content = file.readlines()
    num_msgs = int(config_content[0])
    
    # initialize globals
    for _ in range(num_procs+1):
        dependency.append([False for _ in range(num_procs+1)])
    
    for _ in range(num_procs+1):
        past.append([[] for _ in range(num_msgs + 1)])


    config_content = config_content[1:]
    for line in config_content:
        proc_list = line.split(' ')
        h = int(proc_list[0])
        proc_list = proc_list[1:]

        dependency[h][h] = True
        for depend in proc_list:
            depend = int(depend)
            dependency[h][depend] = True

    if DEBUG:
        print(num_procs)
        print(num_msgs)
        print(np.array(dependency))

parse_args()


# delivered[p][q][m] = True/False tells whether process p has delivered message m of process q
delivered = None  # to be initialised when CL arguments are parsed


# causalPast[p][m] contains the causal past of message m from process p UP TO MESSAGE m-1 OF PROCESS p
# When process p tries to deliver message m from process q, it checks up to message m-1 of process q,
# since messages previously delivered by q are in the causal past of message m-1 form q, and have already been checked for
# def findCausalPast():
#     global causalPast

#     for p in range(1, 1+nProc):
#         # Messages delivered now by p all go in the causal past of the next message process p broadcasts
#         nextMsg = 1
#         with open("da_proc_" + str(p) + ".out") as outFile:
#             for line in outFile:
#                 tokens = line.split()
#                 if tokens[0] == "d":
#                     q = int(tokens[1])
#                     m = int(tokens[2])
#                     if depends[p][q]:
#                         causalPast[p][nextMsg].append((q, m))
#                 elif tokens[0] == "b":
#                     m = int(tokens[1])
#                     assert m == nextMsg
#                     nextMsg += 1
#                     if nextMsg == nMsgs:
#                         return
#                     causalPast[p][nextMsg].append((p, m))
#                 else:
#                     raise Exception("Malformed file:", line)
#     return


# def checkDeliverable(p, q, m):
#     for (qp, mp) in causalPast[q][m]:
#         if not delivered[p][qp][mp]:
#             # print(causalPast[q][m])
#             return False
#     return True


# def checkLocal():
#     global delivered

#     for p in range(1, 1+nProc):
#         filename = "da_proc_" + str(p) + ".out"
#         with open(filename) as outFile:
#             for line in outFile:
#                 tokens = line.split()
#                 if tokens[0] == "d":
#                     q = int(tokens[1])
#                     m = int(tokens[2])
#                     if not checkDeliverable(p, q, m):
#                         return False, filename, line
#                     delivered[p][q][m] = True
#                 elif tokens[0] == "b":
#                     m = int(tokens[1])
#                     delivered[p][p][m] = True
#                 else:
#                     raise Exception("Malformed file",
#                                     "da_proc_" + p + ".out:", line)
#     return True, "bravo"


# parseArguments()
# findDepends()
# findCausalPast()
# res = checkLocal()
# if not res[0]:
#     filename = res[1]
#     line = res[2]
#     print("Test failed")
#     print(filename + ":", line)
# else:
#     print("Test passed")
