package main

import (
	"bufio"
	"fmt"
	"os/exec"
	"time"

	"github.com/radovskyb/watcher"
)

func spawn(kill chan bool) (done chan bool) {
	cmd := exec.Command("lumo", "-c", "src", "src/gggv/show.cljs")

	stdOut, err := cmd.StdoutPipe()
	if err != nil {
		panic(err)
	}
	stdErr, err := cmd.StderrPipe()
	if err != nil {
		panic(err)
	}

	if err := cmd.Start(); err != nil {
		panic(err)
	}

	go func() {
		rd := bufio.NewReader(stdOut)
		killed := false
		for {
			ln, _, err := rd.ReadLine()
			if err != nil {
				break
			}
			if kill != nil && !killed {
				kill <- true
				killed = true
				<-kill
			}
			println(string(ln))
		}
	}()
	go func() {
		rd := bufio.NewReader(stdErr)
		for {
			ln, _, err := rd.ReadLine()
			if err != nil {
				return
			}
			println(string(ln))
		}
	}()
	done = make(chan bool)
	go func() {
		<-done
		cmd.Process.Kill()
		done <- true
		stdOut.Close()
		stdErr.Close()
		close(done)
	}()
	return
}

func main() {
	kill := spawn(nil)
	w := watcher.New()
	if err := w.AddRecursive("src/"); err != nil {
		panic(err)
	}
	go w.Start(300 * time.Millisecond)
	for {
		select {
		case e := <-w.Event:
			fmt.Println(e)
			kill = spawn(kill)
		case err := <-w.Error:
			fmt.Println(err)
		}
	}
}
