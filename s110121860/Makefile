# Makefile 1.1 (GNU Make 3.81; MacOSX gcc 4.2.1; MacOSX MinGW 4.3.0)

PROJ  := s110121860
VA    := 0
VB    := 0
FILES := s110121860Player Sequence
SDIR  := src
BDIR  := bin
BACK  := backup
ICON  := icon.ico
EXTRA := $(SDIR)/Fail.java # $(SDIR)/icon.rc
INST  := $(PROJ)-$(VA)_$(VB)
OBJS  := $(patsubst %,$(BDIR)/%.class,$(FILES))
SRCS  := $(patsubst %,$(SDIR)/%.java,$(FILES))

CC   := javac
CF   := -g:none -O -d $(BDIR) $(SDIR)/*.java -Xlint:unchecked -Xlint:deprecation -cp ../jar/projectsrc.jar:bin/s110121860/s110121860Player.class #-verbose (it's not kidding)
OF   := # -framework OpenGL -framework GLUT

default: $(BDIR)/$(PROJ)

#	cp $(SDIR)/$(ICON) $(BDIR)/$(ICON)
#	sips --addIcon $(BDIR)/$(ICON)
#	DeRez -only icns $(BDIR)/$(ICON) > $(BDIR)/$(ICON).rsrc
#	Rez -append $(BDIR)/$(ICON).rsrc -o $(BDIR)/$(PROJ)
#	SetFile -a C $(BDIR)/$(PROJ)
#	rm $(BDIR)/$(ICON) $(BDIR)/$(ICON).rsrc

#$(BDIR)/$(PROJ): $(OBJS)

$(BDIR)/$(PROJ): $(SRCS)
	@mkdir -p $(BDIR)
	$(CC) $(CF) $?

#	$(CC) $(CF) $(OF) $^ -o $@

$(BDIR)/%.class: $(SDIR)/%.java
	@mkdir -p $(BDIR)
	$(CC) $(CF) -c $? -o $@

.PHONY: setup clean backup sever client kill

server:
	java -cp ../jar/projectsrc.jar boardgame.Server -b halma.CCBoard -t 300000 &
#	sleep 2s
#	java -cp ../jar/projectsrc.jar:./bin boardgame.Client s110121860.s110121860Player

client:
	java -cp ../jar/projectsrc.jar:./bin boardgame.Client s110121860.s110121860Player
	
kill:
	killall -v -SIGKILL java
	sleep 1s
	ps

#	java -cp ../jar/projectsrc.jar:./bin/s110121860.class boardgame.Client s110121860.s110121860Player &
#	java -cp ../jar/projectsrc.jar:./bin/s110121860.class boardgame.Client halma.CCRandomPlayer &
#	java -cp ../jar/projectsrc.jar:./bin/s110121860.class s110121860.s110121860Player &
#	java -cp ../jar/projectsrc.jar:./bin/s110121860.class s110121860.TestClient s110121860.s110121860Player &
#	java -cp ../jar/projectsrc.jar boardgame.Client &

setup: default
	@mkdir -p $(BDIR)/$(INST)
	cp $(BDIR)/$(PROJ) readme.txt gpl.txt copying.txt $(BDIR)/$(INST)
	rm -f $(BDIR)/$(INST)-MacOSX.dmg
	# or rm -f $(BDIR)/$(INST)-Win32.zip
	hdiutil create $(BDIR)/$(INST)-MacOSX.dmg -volname "$(PROJ) $(VA).$(VB)" -srcfolder $(BDIR)/$(INST)
	# or zip $(BDIR)/$(INST)-Win32.zip -r $(BDIR)/$(INST)
	rm -R $(BDIR)/$(INST)

clean:
	-rm -rf $(BDIR)/$(PROJ)
#	-rm -f $(OBJS)

backup:
	@mkdir -p $(BACK)
	zip $(BACK)/$(INST)-`date +%Y-%m-%dT%H%M%S`.zip readme.txt gpl.txt copying.txt Makefile $(SRCS) $(H) $(EXTRA) # $(SDIR)/$(ICON)
