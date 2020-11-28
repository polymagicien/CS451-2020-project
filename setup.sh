#!/bin/bash

SESSION="test"
tmux kill-sess -t $SESSION

cd template_java/

tmux -2 new-session -x 300 -y 80 -d -s $SESSION -n "main"

sudo ip netns del test 2>/dev/null
sudo ip netns add test
sudo ip netns exec test ip link set lo up

# Set windows tcpdump
# tmux new-window -n 'tcpdump'
tmux select-window -t 'main'
# Split window in 4 blocs
#       0  2
#       1  3
tmux split-window -h  
tmux select-pane -t 0
tmux split-window -v
tmux select-pane -t 2
tmux split-window -v

tmux select-pane -t 3
tmux split-window -h
tmux select-pane -t 0
tmux split-window -h

tmux select-pane -t 0
tmux send-keys "sudo ip netns exec test bash" C-m
tmux select-pane -t 1
tmux send-keys "sudo ip netns exec test bash" C-m
tmux select-pane -t 2
tmux send-keys "sudo ip netns exec test bash" C-m
tmux select-pane -t 3
tmux send-keys "sudo ip netns exec test bash" C-m
tmux select-pane -t 4
tmux send-keys "sudo ip netns exec test bash" C-m
tmux select-pane -t 5
tmux send-keys "sudo ip netns exec test bash" C-m


tmux select-pane -t 0
tmux send-keys "../barrier.py --processes 3"
tmux select-pane -t 1
tmux send-keys "../finishedSignal.py --processes 3"
tmux select-pane -t 2
tmux send-keys "tc qdisc add dev lo root netem 2>/dev/null" C-m
tmux send-keys "tc qdisc change dev lo root netem delay 200ms 50ms distribution normal loss 10% 25% reorder 25% 50%"
tmux select-pane -t 3
tmux send-keys "./run.sh --id 1 --hosts ../host --barrier localhost:10000 --signal localhost:11000 --output test1.out ../config"
tmux select-pane -t 4
tmux send-keys "./run.sh --id 2 --hosts ../host --barrier localhost:10000 --signal localhost:11000 --output test2.out ../config"
tmux select-pane -t 5
tmux send-keys "./run.sh --id 3 --hosts ../host --barrier localhost:10000 --signal localhost:11000 --output test3.out ../config"

tmux select-layout tiled
# tmux select-layout even-horizontal
tmux a 