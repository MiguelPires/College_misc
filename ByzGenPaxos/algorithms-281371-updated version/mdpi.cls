%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% %%	MDPI class for LaTeX files	 28.2.2018
%% %%	For any information please send an e-mail to:
%% %%		latex-support@mdpi.com (Dr. Janine Daum) 
%% %%
%% %%	Initial class provided by:
%% %%		Stefano Mariani 
%% %%   Modified by:
%% %%		Dietrich Rordorf 
%% %%		Peter Harremoes 
%% %%		Zeno Schumacher 
%% %%		Maddalena Giulini 
%% %%		Andres Gartmann 
%% %%		Dr. Janine Daum 
%% %%   Versions:
%% %%		v1.0 before Dr. Janine Daum
%% %%		v2.0 when Dr. Janine Daum started (March 2013)
%% %%		v3.0 after layout change (September 2015)
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%% IDENTIFICATION
\NeedsTeXFormat{LaTeX2e}
\ProvidesClass{mdpi}[28.2.2018 MDPI paper class]

%%%% Copyright and citebox
 \AtEndDocument{\par \cright \par \vspace{12pt} \citebox}
 
%% PRELIMINARY DECLARATIONS
\LoadClass[10pt]{article}
\RequirePackage[T1]{fontenc}
\RequirePackage[utf8]{inputenc}
\RequirePackage{calc}
\RequirePackage{indentfirst}
\RequirePackage{fancyhdr}
\RequirePackage{graphicx,epstopdf}
\RequirePackage{lastpage}
\RequirePackage{ifthen}
\RequirePackage{lineno}
\RequirePackage{float}
\RequirePackage{amsmath}
\RequirePackage{setspace}
\RequirePackage{enumitem}
\RequirePackage{mathpazo}
\RequirePackage{booktabs} % For \toprule etc. in tables
\RequirePackage[largestsep]{titlesec}
\RequirePackage{etoolbox} % For \AtBeginDocument etc.
\RequirePackage{tabto} % To use tab for alignment on first page
\RequirePackage{color} % To provide color for soul
\RequirePackage{soul} % To highlight text
\newcommand{\highlight}[1]{\colorbox{yellow}{#1}}
\RequirePackage{multirow}
\RequirePackage{microtype} % For command \textls[]{}
\RequirePackage{tikz} % For Orcid icon
\RequirePackage{siunitx} % For leading zeros in DOI number

 
%% OPTIONS
%% To choose the journal
\DeclareOption{actuators}{\gdef\@journal{actuators}	\gdef\@doiabbr{act}	\gdef\@ISSN{2076-0825}}
\DeclareOption{addictions}{\gdef\@journal{addictions}	\gdef\@doiabbr{}	\gdef\@ISSN{0006-0006}}
\DeclareOption{admsci}{\gdef\@journal{admsci}	\gdef\@doiabbr{admsci}	\gdef\@ISSN{2076-3387}}
\DeclareOption{aerospace}{\gdef\@journal{aerospace}	\gdef\@doiabbr{aerospace}	\gdef\@ISSN{2226-4310}}
\DeclareOption{agriculture}{\gdef\@journal{agriculture}	\gdef\@doiabbr{agriculture}	\gdef\@ISSN{2077-0472}}
\DeclareOption{agronomy}{\gdef\@journal{agronomy}	\gdef\@doiabbr{agronomy}	\gdef\@ISSN{2073-4395}}
\DeclareOption{algorithms}{\gdef\@journal{algorithms}	\gdef\@doiabbr{a}	\gdef\@ISSN{1999-4893}}
\DeclareOption{animals}{\gdef\@journal{animals}	\gdef\@doiabbr{ani}	\gdef\@ISSN{2076-2615}}
\DeclareOption{antibiotics}{\gdef\@journal{antibiotics}	\gdef\@doiabbr{antibiotics}	\gdef\@ISSN{2079-6382}}
\DeclareOption{antibodies}{\gdef\@journal{antibodies}	\gdef\@doiabbr{antib}	\gdef\@ISSN{2073-4468}}
\DeclareOption{antioxidants}{\gdef\@journal{antioxidants}	\gdef\@doiabbr{antiox}	\gdef\@ISSN{2076-3921}}
\DeclareOption{applsci}{\gdef\@journal{applsci}	\gdef\@doiabbr{app}	\gdef\@ISSN{2076-3417}}
\DeclareOption{arts}{\gdef\@journal{arts}	\gdef\@doiabbr{arts}	\gdef\@ISSN{2076-0752}}
\DeclareOption{asi}{\gdef\@journal{asi}	\gdef\@doiabbr{asi}	\gdef\@ISSN{2571-5577}}
\DeclareOption{atmosphere}{\gdef\@journal{atmosphere}	\gdef\@doiabbr{atmos}	\gdef\@ISSN{2073-4433}}
\DeclareOption{atoms}{\gdef\@journal{atoms}	\gdef\@doiabbr{atoms}	\gdef\@ISSN{2218-2004}}
\DeclareOption{axioms}{\gdef\@journal{axioms}	\gdef\@doiabbr{axioms}	\gdef\@ISSN{2075-1680}}
\DeclareOption{batteries}{\gdef\@journal{batteries}	\gdef\@doiabbr{batteries}	\gdef\@ISSN{2313-0105}}
\DeclareOption{bdcc}{\gdef\@journal{bdcc}	\gdef\@doiabbr{bdcc}	\gdef\@ISSN{2504-2289}}
\DeclareOption{behavsci}{\gdef\@journal{behavsci} 	\gdef\@doiabbr{bs}	\gdef\@ISSN{2076-328X}}
\DeclareOption{beverages}{\gdef\@journal{beverages}	\gdef\@doiabbr{beverages}	\gdef\@ISSN{2306-5710}}
\DeclareOption{bioengineering}{\gdef\@journal{bioengineering}	\gdef\@doiabbr{bioengineering}	\gdef\@ISSN{2306-5354}}
\DeclareOption{biology}{\gdef\@journal{biology}	\gdef\@doiabbr{biology}	\gdef\@ISSN{2079-7737}}
\DeclareOption{biomedicines}{\gdef\@journal{biomedicines}	\gdef\@doiabbr{biomedicines}	\gdef\@ISSN{2227-9059}}
\DeclareOption{biomimetics}{\gdef\@journal{biomimetics}	\gdef\@doiabbr{biomimetics}	\gdef\@ISSN{2313-7673}}
\DeclareOption{biomolecules}{\gdef\@journal{biomolecules}	\gdef\@doiabbr{biom}	\gdef\@ISSN{2218-273X}}
\DeclareOption{biosensors}{\gdef\@journal{biosensors}	\gdef\@doiabbr{bios}	\gdef\@ISSN{2079-6374}}
\DeclareOption{brainsci}{\gdef\@journal{brainsci} 	\gdef\@doiabbr{brainsci}	\gdef\@ISSN{2076-3425}}
\DeclareOption{buildings}{\gdef\@journal{buildings}	\gdef\@doiabbr{buildings}	\gdef\@ISSN{2075-5309}}
\DeclareOption{cancers}{\gdef\@journal{cancers}	\gdef\@doiabbr{cancers}	\gdef\@ISSN{2072-6694}}
\DeclareOption{carbon}{\gdef\@journal{carbon} 	\gdef\@doiabbr{c}	\gdef\@ISSN{2311-5629}}
\DeclareOption{catalysts}{\gdef\@journal{catalysts}	\gdef\@doiabbr{catal}	\gdef\@ISSN{2073-4344}}
\DeclareOption{cells}{\gdef\@journal{cells}	\gdef\@doiabbr{cells}	\gdef\@ISSN{2073-4409}}
\DeclareOption{ceramics}{\gdef\@journal{ceramics}	\gdef\@doiabbr{}	\gdef\@ISSN{2571-6131}}
\DeclareOption{challenges}{\gdef\@journal{challenges}	\gdef\@doiabbr{challe}	\gdef\@ISSN{2078-1547}}
\DeclareOption{chemengineering}{\gdef\@journal{chemengineering}	\gdef\@doiabbr{chemengineering}	\gdef\@ISSN{2305-7084}}
\DeclareOption{chemosensors}{\gdef\@journal{chemosensors}	\gdef\@doiabbr{chemosensors}	\gdef\@ISSN{2227-9040}}
\DeclareOption{children}{\gdef\@journal{children}	\gdef\@doiabbr{children}	\gdef\@ISSN{2227-9067}}
\DeclareOption{climate}{\gdef\@journal{climate}	\gdef\@doiabbr{cli}	\gdef\@ISSN{2225-1154}}
\DeclareOption{coatings}{\gdef\@journal{coatings}	\gdef\@doiabbr{coatings}	\gdef\@ISSN{2079-6412}}
\DeclareOption{colloids}{\gdef\@journal{colloids}	\gdef\@doiabbr{colloids}	\gdef\@ISSN{2504-5377}}
\DeclareOption{computation}{\gdef\@journal{computation}	\gdef\@doiabbr{computation}	\gdef\@ISSN{2079-3197}}
\DeclareOption{computers}{\gdef\@journal{computers}	\gdef\@doiabbr{computers}	\gdef\@ISSN{2073-431X}}
\DeclareOption{condensedmatter}{\gdef\@journal{condensedmatter}	\gdef\@doiabbr{condmat}	\gdef\@ISSN{2410-3896}}
\DeclareOption{cosmetics}{\gdef\@journal{cosmetics}	\gdef\@doiabbr{cosmetics}	\gdef\@ISSN{2079-9284}}
\DeclareOption{cryptography}{\gdef\@journal{cryptography}	\gdef\@doiabbr{cryptography}	\gdef\@ISSN{2410-387X}}
\DeclareOption{crystals}{\gdef\@journal{crystals}	\gdef\@doiabbr{cryst}	\gdef\@ISSN{2073-4352}}
\DeclareOption{cybersecurity}{\gdef\@journal{cybersecurity}	\gdef\@doiabbr{}	\gdef\@ISSN{0009-0009}}
\DeclareOption{data}{\gdef\@journal{data}	\gdef\@doiabbr{data}	\gdef\@ISSN{2306-5729}}
\DeclareOption{dentistry}{\gdef\@journal{dentistry}	\gdef\@doiabbr{dj}	\gdef\@ISSN{2304-6767}}
\DeclareOption{designs}{\gdef\@journal{designs} 	\gdef\@doiabbr{designs}	\gdef\@ISSN{2411-9660}}
\DeclareOption{diagnostics}{\gdef\@journal{diagnostics}	\gdef\@doiabbr{diagnostics}	\gdef\@ISSN{2075-4418}}
\DeclareOption{diseases}{\gdef\@journal{diseases}	\gdef\@doiabbr{diseases}	\gdef\@ISSN{2079-9721}}
\DeclareOption{diversity}{\gdef\@journal{diversity}	\gdef\@doiabbr{d}	\gdef\@ISSN{1424-2818}}
\DeclareOption{drones}{\gdef\@journal{drones}	\gdef\@doiabbr{drones}	\gdef\@ISSN{2504-446X}}
\DeclareOption{econometrics}{\gdef\@journal{econometrics}	\gdef\@doiabbr{econometrics}	\gdef\@ISSN{2225-1146}}
\DeclareOption{economies}{\gdef\@journal{economies}	\gdef\@doiabbr{economies}	\gdef\@ISSN{2227-7099}}
\DeclareOption{education}{\gdef\@journal{education}	\gdef\@doiabbr{educsci}	\gdef\@ISSN{2227-7102}}
\DeclareOption{electrochem}{\gdef\@journal{electrochem}	\gdef\@doiabbr{}	\gdef\@ISSN{}}
\DeclareOption{electronics}{\gdef\@journal{electronics}	\gdef\@doiabbr{electronics}	\gdef\@ISSN{2079-9292}}
\DeclareOption{energies}{\gdef\@journal{energies}	\gdef\@doiabbr{en}	\gdef\@ISSN{1996-1073}}
\DeclareOption{entropy}{\gdef\@journal{entropy}	\gdef\@doiabbr{e}	\gdef\@ISSN{1099-4300}}
\DeclareOption{environments}{\gdef\@journal{environments}	\gdef\@doiabbr{environments}	\gdef\@ISSN{2076-3298}}
\DeclareOption{epigenomes}{\gdef\@journal{epigenomes}	\gdef\@doiabbr{epigenomes}	\gdef\@ISSN{2075-4655}}
\DeclareOption{est}{\gdef\@journal{est}	\gdef\@doiabbr{}	\gdef\@ISSN{}}
\DeclareOption{fermentation}{\gdef\@journal{fermentation}	\gdef\@doiabbr{fermentation}	\gdef\@ISSN{2311-5637}}
\DeclareOption{fibers}{\gdef\@journal{fibers}	\gdef\@doiabbr{fib}	\gdef\@ISSN{2079-6439}}
\DeclareOption{fire}{\gdef\@journal{fire}	\gdef\@doiabbr{fire}	\gdef\@ISSN{2571-6255}}
\DeclareOption{fishes}{\gdef\@journal{fishes}	\gdef\@doiabbr{fishes}	\gdef\@ISSN{2410-3888}}
\DeclareOption{fluids}{\gdef\@journal{fluids}	\gdef\@doiabbr{fluids}	\gdef\@ISSN{2311-5521}}
\DeclareOption{foods}{\gdef\@journal{foods}	\gdef\@doiabbr{foods}	\gdef\@ISSN{2304-8158}}
\DeclareOption{forecasting}{\gdef\@journal{forecasting}	\gdef\@doiabbr{}	\gdef\@ISSN{}}
\DeclareOption{forests}{\gdef\@journal{forests}	\gdef\@doiabbr{f}	\gdef\@ISSN{1999-4907}}
\DeclareOption{fractalfract}{\gdef\@journal{fractalfract}	\gdef\@doiabbr{fractalfract}	\gdef\@ISSN{2504-3110}}
\DeclareOption{futureinternet}{\gdef\@journal{futureinternet}	\gdef\@doiabbr{fi}	\gdef\@ISSN{1999-5903}}
\DeclareOption{galaxies}{\gdef\@journal{galaxies}	\gdef\@doiabbr{galaxies}	\gdef\@ISSN{2075-4434}}
\DeclareOption{games}{\gdef\@journal{games}	\gdef\@doiabbr{g}	\gdef\@ISSN{2073-4336}}
\DeclareOption{gastrointestdisord}{\gdef\@journal{gastrointestdisord}	\gdef\@doiabbr{}	\gdef\@ISSN{}}
\DeclareOption{gels}{\gdef\@journal{gels}	\gdef\@doiabbr{gels}	\gdef\@ISSN{2310-2861}}
\DeclareOption{genealogy}{\gdef\@journal{genealogy}	\gdef\@doiabbr{genealogy}	\gdef\@ISSN{2313-5778}}
\DeclareOption{genes}{\gdef\@journal{genes}	\gdef\@doiabbr{genes}	\gdef\@ISSN{2073-4425}}
\DeclareOption{geosciences}{\gdef\@journal{geosciences}	\gdef\@doiabbr{geosciences}	\gdef\@ISSN{2076-3263}}
\DeclareOption{geriatrics}{\gdef\@journal{geriatrics}	\gdef\@doiabbr{geriatrics}	\gdef\@ISSN{2308-3417}}
\DeclareOption{hazardousmatters}{\gdef\@journal{hazardousmatters}	\gdef\@doiabbr{}	\gdef\@ISSN{0014-0014}}
\DeclareOption{healthcare}{\gdef\@journal{healthcare}	\gdef\@doiabbr{healthcare}	\gdef\@ISSN{2227-9032}}
\DeclareOption{heritage}{\gdef\@journal{heritage}	\gdef\@doiabbr{}	\gdef\@ISSN{}}
\DeclareOption{highthroughput}{\gdef\@journal{highthroughput}	\gdef\@doiabbr{ht}	\gdef\@ISSN{2571-5135}}
\DeclareOption{horticulturae}{\gdef\@journal{horticulturae}	\gdef\@doiabbr{horticulturae}	\gdef\@ISSN{2311-7524}}
\DeclareOption{humanities}{\gdef\@journal{humanities}	\gdef\@doiabbr{h}	\gdef\@ISSN{2076-0787}}
\DeclareOption{hydrology}{\gdef\@journal{hydrology}	\gdef\@doiabbr{hydrology}	\gdef\@ISSN{2306-5338}}
\DeclareOption{ijerph}{\gdef\@journal{ijerph}	\gdef\@doiabbr{ijerph}	\gdef\@ISSN{1660-4601}}
\DeclareOption{ijfs}{\gdef\@journal{ijfs}	\gdef\@doiabbr{ijfs}	\gdef\@ISSN{2227-7072}}
\DeclareOption{ijgi}{\gdef\@journal{ijgi}	\gdef\@doiabbr{ijgi}	\gdef\@ISSN{2220-9964}}
\DeclareOption{ijms}{\gdef\@journal{ijms}	\gdef\@doiabbr{ijms}	\gdef\@ISSN{1422-0067}}
\DeclareOption{ijtpp}{\gdef\@journal{ijtpp}	\gdef\@doiabbr{ijtpp}	\gdef\@ISSN{2504-186X}}
\DeclareOption{informatics}{\gdef\@journal{informatics}	\gdef\@doiabbr{informatics}	\gdef\@ISSN{2227-9709}}
\DeclareOption{information}{\gdef\@journal{information}	\gdef\@doiabbr{info}	\gdef\@ISSN{2078-2489}}
\DeclareOption{infrastructures}{\gdef\@journal{infrastructures}	\gdef\@doiabbr{infrastructures}	\gdef\@ISSN{2412-3811}}
\DeclareOption{inorganics}{\gdef\@journal{inorganics}	\gdef\@doiabbr{inorganics}	\gdef\@ISSN{2304-6740}}
\DeclareOption{insects}{\gdef\@journal{insects}	\gdef\@doiabbr{insects}	\gdef\@ISSN{2075-4450}}
\DeclareOption{instruments}{\gdef\@journal{instruments}	\gdef\@doiabbr{instruments}	\gdef\@ISSN{2410-390X}}
\DeclareOption{inventions}{\gdef\@journal{inventions}	\gdef\@doiabbr{inventions}	\gdef\@ISSN{2411-5134}}
\DeclareOption{j}{\gdef\@journal{jjournal}	\gdef\@doiabbr{}	\gdef\@ISSN{1234-1357}}
\DeclareOption{jcdd}{\gdef\@journal{jcdd}	\gdef\@doiabbr{jcdd}	\gdef\@ISSN{2308-3425}}
\DeclareOption{jcm}{\gdef\@journal{jcm}	\gdef\@doiabbr{jcm}	\gdef\@ISSN{2077-0383}}
\DeclareOption{jcs}{\gdef\@journal{jcs}	\gdef\@doiabbr{jcs}	\gdef\@ISSN{2504-477X}}
\DeclareOption{jdb}{\gdef\@journal{jdb}	\gdef\@doiabbr{jdb}	\gdef\@ISSN{2221-3759}}
\DeclareOption{jfb}{\gdef\@journal{jfb}	\gdef\@doiabbr{jfb}	\gdef\@ISSN{2079-4983}}
\DeclareOption{jfmk}{\gdef\@journal{jfmk}	\gdef\@doiabbr{jfmk}	\gdef\@ISSN{2411-5142}}
\DeclareOption{jimaging}{\gdef\@journal{jimaging}	\gdef\@doiabbr{jimaging}	\gdef\@ISSN{2313-433X}}
\DeclareOption{jintelligence}{\gdef\@journal{jintelligence}	\gdef\@doiabbr{jintelligence}	\gdef\@ISSN{2079-3200}}
\DeclareOption{jlpea}{\gdef\@journal{jlpea}	\gdef\@doiabbr{jlpea}	\gdef\@ISSN{2079-9268}}
\DeclareOption{jmmp}{\gdef\@journal{jmmp}	\gdef\@doiabbr{jmmp}	\gdef\@ISSN{2504-4494}}
\DeclareOption{jmse}{\gdef\@journal{jmse}	\gdef\@doiabbr{jmse}	\gdef\@ISSN{2077-1312}}
\DeclareOption{jof}{\gdef\@journal{jof}	\gdef\@doiabbr{jof}	\gdef\@ISSN{2309-608X}}
\DeclareOption{joitmc}{\gdef\@journal{joitmc}	\gdef\@doiabbr{}	\gdef\@ISSN{}}
\DeclareOption{jpm}{\gdef\@journal{jpm}	\gdef\@doiabbr{jpm}	\gdef\@ISSN{2075-4426}}
\DeclareOption{jrfm}{\gdef\@journal{jrfm}	\gdef\@doiabbr{jrfm}	\gdef\@ISSN{1911-8074}}
\DeclareOption{jsan}{\gdef\@journal{jsan}	\gdef\@doiabbr{jsan}	\gdef\@ISSN{2224-2708}}
\DeclareOption{land}{\gdef\@journal{land}	\gdef\@doiabbr{land}	\gdef\@ISSN{2073-445X}}
\DeclareOption{languages}{\gdef\@journal{languages}	\gdef\@doiabbr{languages}	\gdef\@ISSN{2226-471X}}
\DeclareOption{laws}{\gdef\@journal{laws}	\gdef\@doiabbr{laws}	\gdef\@ISSN{2075-471X}}
\DeclareOption{life}{\gdef\@journal{life}	\gdef\@doiabbr{life}	\gdef\@ISSN{2075-1729}}
\DeclareOption{literature}{\gdef\@journal{literature}	\gdef\@doiabbr{}	\gdef\@ISSN{2410-9789}}
\DeclareOption{logistics}{\gdef\@journal{logistics}	\gdef\@doiabbr{logistics}	\gdef\@ISSN{2305-6290}}
\DeclareOption{lubricants}{\gdef\@journal{lubricants}	\gdef\@doiabbr{lubricants}	\gdef\@ISSN{2075-4442}}
\DeclareOption{machines}{\gdef\@journal{machines}	\gdef\@doiabbr{machines}	\gdef\@ISSN{2075-1702}}
\DeclareOption{magnetochemistry}{\gdef\@journal{magnetochemistry}	\gdef\@doiabbr{magnetochemistry}	\gdef\@ISSN{2312-7481}}
\DeclareOption{make}{\gdef\@journal{make}	\gdef\@doiabbr{make}	\gdef\@ISSN{2504-4990}}
\DeclareOption{marinedrugs}{\gdef\@journal{marinedrugs}	\gdef\@doiabbr{md}	\gdef\@ISSN{1660-3397}}
\DeclareOption{materials}{\gdef\@journal{materials}	\gdef\@doiabbr{ma}	\gdef\@ISSN{1996-1944}}
\DeclareOption{mathematics}{\gdef\@journal{mathematics}	\gdef\@doiabbr{math}	\gdef\@ISSN{2227-7390}}
\DeclareOption{mca}{\gdef\@journal{mca}	\gdef\@doiabbr{mca}	\gdef\@ISSN{2297-8747}}
\DeclareOption{medicines}{\gdef\@journal{medicines}	\gdef\@doiabbr{medicines}	\gdef\@ISSN{2305-6320}}
\DeclareOption{medsci}{\gdef\@journal{medsci}	\gdef\@doiabbr{medsci}	\gdef\@ISSN{2076-3271}}
\DeclareOption{membranes}{\gdef\@journal{membranes}	\gdef\@doiabbr{membranes}	\gdef\@ISSN{2077-0375}}
\DeclareOption{metabolites}{\gdef\@journal{metabolites}	\gdef\@doiabbr{metabo}	\gdef\@ISSN{2218-1989}}
\DeclareOption{metals}{\gdef\@journal{metals}	\gdef\@doiabbr{met}	\gdef\@ISSN{2075-4701}}
\DeclareOption{microarrays}{\gdef\@journal{microarrays}	\gdef\@doiabbr{}	\gdef\@ISSN{2076-3905}}
\DeclareOption{micromachines}{\gdef\@journal{micromachines}	\gdef\@doiabbr{mi}	\gdef\@ISSN{2072-666X}}
\DeclareOption{microorganisms}{\gdef\@journal{microorganisms}	\gdef\@doiabbr{microorganisms}	\gdef\@ISSN{2076-2607}}
\DeclareOption{minerals}{\gdef\@journal{minerals}	\gdef\@doiabbr{min}	\gdef\@ISSN{2075-163X}}
\DeclareOption{modelling}{\gdef\@journal{modelling}	\gdef\@doiabbr{}	\gdef\@ISSN{0012-0012}}
\DeclareOption{molbank}{\gdef\@journal{molbank}	\gdef\@doiabbr{M}	\gdef\@ISSN{1422-8599}}
\DeclareOption{molecules}{\gdef\@journal{molecules}	\gdef\@doiabbr{molecules}	\gdef\@ISSN{1420-3049}}
\DeclareOption{mps}{\gdef\@journal{mps}	\gdef\@doiabbr{mps}	\gdef\@ISSN{2409-9279}}
\DeclareOption{mti}{\gdef\@journal{mti}	\gdef\@doiabbr{mti}	\gdef\@ISSN{2414-4088}}
\DeclareOption{nanomaterials}{\gdef\@journal{nanomaterials}	\gdef\@doiabbr{nano}	\gdef\@ISSN{2079-4991}}
\DeclareOption{ncrna}{\gdef\@journal{ncrna}	\gdef\@doiabbr{ncrna}	\gdef\@ISSN{2311-553X}}
\DeclareOption{neonatalscreening}{\gdef\@journal{neonatalscreening}	\gdef\@doiabbr{ijns}	\gdef\@ISSN{2409-515X}}
\DeclareOption{neuroglia}{\gdef\@journal{neuroglia}	\gdef\@doiabbr{neuroglia}	\gdef\@ISSN{}}
\DeclareOption{nitrogen}{\gdef\@journal{nitrogen}	\gdef\@doiabbr{}	\gdef\@ISSN{2504-3129}}
\DeclareOption{notspecified}{\gdef\@journal{notspecified}	\gdef\@doiabbr{}	\gdef\@ISSN{}}
\DeclareOption{nutrients}{\gdef\@journal{nutrients}	\gdef\@doiabbr{nu}	\gdef\@ISSN{2072-6643}}
\DeclareOption{ohbm}{\gdef\@journal{ohbm}	\gdef\@doiabbr{ohbm}	\gdef\@ISSN{2504-463X}}
\DeclareOption{particles}{\gdef\@journal{particles}	\gdef\@doiabbr{particles}	\gdef\@ISSN{2571-712X}}
\DeclareOption{pathogens}{\gdef\@journal{pathogens}	\gdef\@doiabbr{pathogens}	\gdef\@ISSN{2076-0817}}
\DeclareOption{pharmaceuticals}{\gdef\@journal{pharmaceuticals}	\gdef\@doiabbr{ph}	\gdef\@ISSN{1424-8247}}
\DeclareOption{pharmaceutics}{\gdef\@journal{pharmaceutics}	\gdef\@doiabbr{pharmaceutics}	\gdef\@ISSN{1999-4923}}
\DeclareOption{pharmacy}{\gdef\@journal{pharmacy}	\gdef\@doiabbr{pharmacy}	\gdef\@ISSN{2226-4787}}
\DeclareOption{philosophies}{\gdef\@journal{philosophies}	\gdef\@doiabbr{philosophies}	\gdef\@ISSN{2409-9287}}
\DeclareOption{photonics}{\gdef\@journal{photonics}	\gdef\@doiabbr{photonics}	\gdef\@ISSN{2304-6732}}
\DeclareOption{plants}{\gdef\@journal{plants}	\gdef\@doiabbr{plants}	\gdef\@ISSN{2223-7747}}
\DeclareOption{plasma}{\gdef\@journal{plasma}	\gdef\@doiabbr{plasma}	\gdef\@ISSN{2571-6182}}
\DeclareOption{polymers}{\gdef\@journal{polymers}	\gdef\@doiabbr{polym}	\gdef\@ISSN{2073-4360}}
\DeclareOption{polysaccharides}{\gdef\@journal{polysaccharides}	\gdef\@doiabbr{}	\gdef\@ISSN{}}
\DeclareOption{preprints}{\gdef\@journal{preprints} 	\gdef\@doiabbr{}	\gdef\@ISSN{}}
\DeclareOption{proceedings}{\gdef\@journal{proceedings}	\gdef\@doiabbr{proceedings}	\gdef\@ISSN{2504-3900}}
\DeclareOption{processes}{\gdef\@journal{processes}	\gdef\@doiabbr{pr}	\gdef\@ISSN{2227-9717}}
\DeclareOption{proteomes}{\gdef\@journal{proteomes}	\gdef\@doiabbr{proteomes}	\gdef\@ISSN{2227-7382}}
\DeclareOption{publications}{\gdef\@journal{publications}	\gdef\@doiabbr{publications}	\gdef\@ISSN{2304-6775}}
\DeclareOption{quaternary}{\gdef\@journal{quaternary}	\gdef\@doiabbr{}	\gdef\@ISSN{2571-550X}}
\DeclareOption{qubs}{\gdef\@journal{qubs}	\gdef\@doiabbr{qubs}	\gdef\@ISSN{2412-382X}}
\DeclareOption{reactions}{\gdef\@journal{reactions}	\gdef\@doiabbr{}	\gdef\@ISSN{}}
\DeclareOption{recycling}{\gdef\@journal{recycling}	\gdef\@doiabbr{recycling}	\gdef\@ISSN{2313-4321}}
\DeclareOption{religions}{\gdef\@journal{religions}	\gdef\@doiabbr{rel}	\gdef\@ISSN{2077-1444}}
\DeclareOption{remotesensing}{\gdef\@journal{remotesensing}	\gdef\@doiabbr{rs}	\gdef\@ISSN{2072-4292}}
\DeclareOption{reports}{\gdef\@journal{reports}	\gdef\@doiabbr{reports}	\gdef\@ISSN{}}
\DeclareOption{resources}{\gdef\@journal{resources}	\gdef\@doiabbr{resources}	\gdef\@ISSN{2079-9276}}
\DeclareOption{risks}{\gdef\@journal{risks}	\gdef\@doiabbr{risks}	\gdef\@ISSN{2227-9091}}
\DeclareOption{robotics}{\gdef\@journal{robotics}	\gdef\@doiabbr{robotics}	\gdef\@ISSN{2218-6581}}
\DeclareOption{safety}{\gdef\@journal{safety}	\gdef\@doiabbr{safety}	\gdef\@ISSN{2313-576X}}
\DeclareOption{scipharm}{\gdef\@journal{scipharm}	\gdef\@doiabbr{scipharm}	\gdef\@ISSN{2218-0532}}
\DeclareOption{sensors}{\gdef\@journal{sensors}	\gdef\@doiabbr{s}	\gdef\@ISSN{1424-8220}}
\DeclareOption{separations}{\gdef\@journal{separations}	\gdef\@doiabbr{separations}	\gdef\@ISSN{2297-8739}}
\DeclareOption{sexes}{\gdef\@journal{sexes}	\gdef\@doiabbr{}	\gdef\@ISSN{2411-5118}}
\DeclareOption{sinusitis}{\gdef\@journal{sinusitis}	\gdef\@doiabbr{sinusitis}	\gdef\@ISSN{2309-107X}}
\DeclareOption{societies}{\gdef\@journal{societies}	\gdef\@doiabbr{soc}	\gdef\@ISSN{2075-4698}}
\DeclareOption{socsci}{\gdef\@journal{socsci}	\gdef\@doiabbr{socsci}	\gdef\@ISSN{2076-0760}}
\DeclareOption{soilsystems}{\gdef\@journal{soilsystems}	\gdef\@doiabbr{}	\gdef\@ISSN{2411-5126}}
\DeclareOption{sports}{\gdef\@journal{sports}	\gdef\@doiabbr{sports}	\gdef\@ISSN{2075-4663}}
\DeclareOption{standards}{\gdef\@journal{standards}	\gdef\@doiabbr{}	\gdef\@ISSN{2305-6703}}
\DeclareOption{stats}{\gdef\@journal{stats}	\gdef\@doiabbr{}	\gdef\@ISSN{}}
\DeclareOption{surfaces}{\gdef\@journal{surfaces}	\gdef\@doiabbr{}	\gdef\@ISSN{}}
\DeclareOption{surgeries}{\gdef\@journal{surgeries}	\gdef\@doiabbr{}	\gdef\@ISSN{2017-2017}}
\DeclareOption{sustainability}{\gdef\@journal{sustainability}	\gdef\@doiabbr{su}	\gdef\@ISSN{2071-1050}}
\DeclareOption{symmetry}{\gdef\@journal{symmetry}	\gdef\@doiabbr{sym}	\gdef\@ISSN{2073-8994}}
\DeclareOption{systems}{\gdef\@journal{systems}	\gdef\@doiabbr{systems}	\gdef\@ISSN{2079-8954}}
\DeclareOption{technologies}{\gdef\@journal{technologies}	\gdef\@doiabbr{technologies}	\gdef\@ISSN{2227-7080}}
\DeclareOption{test}{\gdef\@journal{test}	\gdef\@doiabbr{}	\gdef\@ISSN{}}
\DeclareOption{toxics}{\gdef\@journal{toxics}	\gdef\@doiabbr{toxics}	\gdef\@ISSN{2305-6304}}
\DeclareOption{toxins}{\gdef\@journal{toxins}	\gdef\@doiabbr{toxins}	\gdef\@ISSN{2072-6651}}
\DeclareOption{tropicalmed}{\gdef\@journal{tropicalmed}	\gdef\@doiabbr{tropicalmed}	\gdef\@ISSN{2414-6366}}
\DeclareOption{universe}{\gdef\@journal{universe}	\gdef\@doiabbr{universe}	\gdef\@ISSN{2218-1997}}
\DeclareOption{urbansci}{\gdef\@journal{urbansci}	\gdef\@doiabbr{urbansci}	\gdef\@ISSN{2413-8851}}
\DeclareOption{vaccines}{\gdef\@journal{vaccines}	\gdef\@doiabbr{vaccines}	\gdef\@ISSN{2076-393X}}
\DeclareOption{vehicles}{\gdef\@journal{vehicles}	\gdef\@doiabbr{}	\gdef\@ISSN{}}
\DeclareOption{vetsci}{\gdef\@journal{vetsci}	\gdef\@doiabbr{vetsci}	\gdef\@ISSN{2306-7381}}
\DeclareOption{vibration}{\gdef\@journal{vibration}	\gdef\@doiabbr{vibration}	\gdef\@ISSN{2571-631X}}
\DeclareOption{viruses}{\gdef\@journal{viruses}	\gdef\@doiabbr{v}	\gdef\@ISSN{1999-4915}}
\DeclareOption{vision}{\gdef\@journal{vision}	\gdef\@doiabbr{vision}	\gdef\@ISSN{2411-5150}}
\DeclareOption{water}{\gdef\@journal{water}	\gdef\@doiabbr{w}	\gdef\@ISSN{2073-4441}}
\DeclareOption{wem}{\gdef\@journal{wem}	\gdef\@doiabbr{}	\gdef\@ISSN{1234-4321}}

\DeclareOption{journal}{\ClassWarning{mdpi}{You have not specified the journal. The first class option of the document class mdpi specifies the journal. The word journal should be replaced by one of the following journal names: actuators, addictions, admsci, aerospace, agriculture, agronomy, algorithms, animals, antibiotics, antibodies, antioxidants, applsci, arts, asi, atmosphere, atoms, axioms, batteries, bdcc, behavsci, beverages, bioengineering, biology, biomedicines, biomimetics, biomolecules, biosensors, brainsci, buildings, carbon, cancers, catalysts, cells, ceramics, challenges, chemengineering, chemosensors, children, climate, coatings, colloids, computation, computers, condensedmatter, cosmetics, cryptography, crystals, cybersecurity, data, dentistry, designs, diagnostics, diseases, diversity, drones, econometrics, economies, education, electrochem, electrochemistry, electronics, energies, entropy, environments, epigenomes, est, fermentation, fibers, fire, fishes, fluids, foods, forecasting, forests, fractalfract, futureinternet, galaxies, games, gastrointestdisord, gels, genealogy, genes, geosciences, geriatrics, hazardousmatters, healthcare, heritage, highthroughput, horticulturae, humanities, hydrology, informatics, information, infrastructures, inorganics, insects, instruments, ijerph, ijfs, ijms, ijgi, ijtpp, inventions, j, jcdd, jcm, jcs, jdb, jfb, jfmk, jimaging, jof, jintelligence, jlpea, jmmp, jmse, jpm, jrfm, jsan, land, languages, laws, life, literature, logistics, lubricants, machines, magnetochemistry, make, marinedrugs, materials, mathematics, mca, medsci, medicines, membranes, metabolites, metals, microarrays, micromachines, microorganisms, minerals, modelling, molbank, molecules, mps, mti, nanomaterials, ncrna, neonatalscreening, neuroglia, nitrogen, nutrients, ohbm, particles, pathogens, pharmaceuticals, pharmaceutics, pharmacy, philosophies, photonics, plants, plasma, polymers, polysaccharides, proceedings, processes, proteomes, publications, quaternary, qubs, reactions, recycling, religions, remotesensing, reports, resources, risks, robotics, safety, scipharm, sensors, separations, sexes, sinusitis, socsci, societies, soilsystems, sports, standards, stats, surfaces, surgeries, sustainability, symmetry, systems, technologies, toxics, toxins, tropicalmed, universe, urbansci, vaccines, vehicles, vetsci, vibration, viruses, vision, water,wem}} 

%% To choose the type of manuscript
\DeclareOption{abstract}{\gdef\@arttype{Abstract}}
\DeclareOption{addendum}{\gdef\@arttype{Addendum}}
\DeclareOption{article}{\gdef\@arttype{Article}}
\DeclareOption{benchmark}{\gdef\@arttype{Benchmark}}
\DeclareOption{book}{\gdef\@arttype{Book}}
\DeclareOption{bookreview}{\gdef\@arttype{Book Review}}
\DeclareOption{briefreport}{\gdef\@arttype{Brief Report}}
\DeclareOption{casereport}{\gdef\@arttype{Case Report}}
\DeclareOption{changes}{\gdef\@arttype{Changes}}
\DeclareOption{comment}{\gdef\@arttype{Comment}}
\DeclareOption{commentary}{\gdef\@arttype{Commentary}}
\DeclareOption{communication}{\gdef\@arttype{Communication}}
\DeclareOption{conceptpaper}{\gdef\@arttype{Concept Paper}}
\DeclareOption{conferenceproceedings}{\gdef\@arttype{Proceedings}}
\DeclareOption{correction}{\gdef\@arttype{Correction}}
\DeclareOption{conferencereport}{\gdef\@arttype{Conference Report}}
\DeclareOption{expressionofconcern}{\gdef\@arttype{Expression of Concern}}
\DeclareOption{meetingreport}{\gdef\@arttype{Meeting Report}}
\DeclareOption{creative}{\gdef\@arttype{Creative}}
\DeclareOption{datadescriptor}{\gdef\@arttype{Data Descriptor}}
\DeclareOption{discussion}{\gdef\@arttype{Discussion}}
\DeclareOption{editorial}{\gdef\@arttype{Editorial}}
\DeclareOption{essay}{\gdef\@arttype{Essay}}
\DeclareOption{erratum}{\gdef\@arttype{Erratum}}
\DeclareOption{hypothesis}{\gdef\@arttype{Hypothesis}}
\DeclareOption{interestingimages}{\gdef\@arttype{Interesting Images}}
\DeclareOption{letter}{\gdef\@arttype{Letter}}
\DeclareOption{meetingreport}{\gdef\@arttype{Meeting Report}}
\DeclareOption{newbookreceived}{\gdef\@arttype{New Book Received}}
\DeclareOption{obituary}{\gdef\@arttype{Obituary}}
\DeclareOption{opinion}{\gdef\@arttype{Opinion}}
\DeclareOption{projectreport}{\gdef\@arttype{Project Report}}
\DeclareOption{reply}{\gdef\@arttype{Reply}}
\DeclareOption{reprint}{\gdef\@arttype{Reprint}}
\DeclareOption{retraction}{\gdef\@arttype{Retraction}}
\DeclareOption{review}{\gdef\@arttype{Review}}
\DeclareOption{perspective}{\gdef\@arttype{Perspective}} 
\DeclareOption{protocol}{\gdef\@arttype{Protocol}} 
\DeclareOption{shortnote}{\gdef\@arttype{Short Note}}
\DeclareOption{supfile}{\gdef\@arttype{Supfile}}
\DeclareOption{technicalnote}{\gdef\@arttype{Technical Note}}
\DeclareOption{viewpoint}{\gdef\@arttype{Viewpoint}}

%% To choose the status of the manuscript
\DeclareOption{submit}{\gdef\@status{submit}}
\DeclareOption{accept}{\gdef\@status{accept}}

%% To choose the whether there is one or more authors
\DeclareOption{oneauthor}{\gdef\@authornum{author}}
\DeclareOption{moreauthors}{\gdef\@authornum{authors}}

%% Add the chosen options to the class
\DeclareOption*{\PassOptionsToClass{\CurrentOption}{article}}

%% Defaults
\ExecuteOptions{notspecified,10pt,a4paper,article,submit,oneauthor}

%% Process options
\ProcessOptions\relax

%% MORE DECLARATIONS
%%%% Maths environments
\RequirePackage{amsthm}
\newtheoremstyle{mdpi}% name
{12pt}% space above
{12pt}% space below
{\itshape}% body font
{}% indent amount 1
{\bfseries}% theorem head font
{.}% punctuation after theorem head
{.5em}% space after theorem head
{}% theorem head spec (can be left empty, meaning `normal')

\renewcommand{\qed}{\unskip\nobreak\quad\qedsymbol} %% This places the symbol right after the text instead of placing it at the end on the line.

\renewenvironment{proof}[1][\proofname]{\par %% \proofname allows to have "Proof of my theorem"
  \pushQED{\qed}%
  \normalfont \topsep6\p@\@plus6\p@\relax
  \trivlist
  \item[\hskip\labelsep
        \bfseries %% "Proof" is bold
    #1\@addpunct{.}]\ignorespaces %% Period instead of colon
}{%
  \popQED\endtrivlist\@endpefalse
}

 \theoremstyle{mdpi}
 \newcounter{theorem}
 \setcounter{theorem}{0}
 \newtheorem{Theorem}[theorem]{Theorem}
 
 \newcounter{lemma}
 \setcounter{lemma}{0}
 \newtheorem{Lemma}[lemma]{Lemma}
 
 \newcounter{corollary}
 \setcounter{corollary}{0}
 \newtheorem{Corollary}[corollary]{Corollary}
 
 \newcounter{proposition}
 \setcounter{proposition}{0}
 \newtheorem{Proposition}[proposition]{Proposition}
 
 \newcounter{characterization}
 \setcounter{characterization}{0}
 \newtheorem{Characterization}[characterization]{Characterization}
 
 \newcounter{property}
 \setcounter{property}{0}
 \newtheorem{Property}[property]{Property}
 
 \newcounter{problem}
 \setcounter{problem}{0}
 \newtheorem{Problem}[problem]{Problem}
 
 \newcounter{example}
 \setcounter{example}{0}
 \newtheorem{Example}[example]{Example}
 
 \newcounter{examplesanddefinitions}
 \setcounter{examplesanddefinitions}{0}
 \newtheorem{ExamplesandDefinitions}[examplesanddefinitions]{Examples and Definitions}
 
 \newcounter{remark}
 \setcounter{remark}{0}
 \newtheorem{Remark}[remark]{Remark}
 
 \newcounter{definition}
 \setcounter{definition}{0}
 \newtheorem{Definition}[definition]{Definition}
 
 \newcounter{hypothesis}
 \setcounter{hypothesis}{0}
 \newtheorem{Hypothesis}[hypothesis]{Hypothesis}

 \newcounter{notation}
 \setcounter{notation}{0}
 \newtheorem{Notation}[notation]{Notation}
 
%%%% Hyphenation
\RequirePackage[none]{hyphenat}
\sloppy

%%%% References
\RequirePackage[sort&compress]{natbib}

\ifthenelse{\equal{\@journal}{admsci}
\OR \equal{\@journal}{arts}
\OR \equal{\@journal}{econometrics}
\OR \equal{\@journal}{economies}
\OR \equal{\@journal}{genealogy}
\OR \equal{\@journal}{humanities}
\OR \equal{\@journal}{ijfs}
\OR \equal{\@journal}{jrfm}
\OR \equal{\@journal}{languages}
\OR \equal{\@journal}{laws}
\OR \equal{\@journal}{religions}
\OR \equal{\@journal}{risks}
\OR \equal{\@journal}{socsci}}{%
	\bibliographystyle{chicago2}
	\bibpunct{(}{)}{;}{x}{}{}%
	}{%
	\bibliographystyle{mdpi}
	\bibpunct{[}{]}{,}{n}{}{}%
	}%

\renewcommand\NAT@set@cites{%
  \ifNAT@numbers
    \ifNAT@super \let\@cite\NAT@citesuper
       \def\NAT@mbox##1{\unskip\nobreak\textsuperscript{##1}}%
       \let\citeyearpar=\citeyear
       \let\NAT@space\relax
       \def\NAT@super@kern{\kern\p@}%
    \else
       \let\NAT@mbox=\mbox
       \let\@cite\NAT@citenum
       \let\NAT@space\relax
       \let\NAT@super@kern\relax
    \fi
    \let\@citex\NAT@citexnum
    \let\@biblabel\NAT@biblabelnum
    \let\@bibsetup\NAT@bibsetnum
    \renewcommand\NAT@idxtxt{\NAT@name\NAT@spacechar\NAT@open\NAT@num\NAT@close}%
    \def\natexlab##1{}%
    \def\NAT@penalty{\penalty\@m}%
  \else
    \let\@cite\NAT@cite
    \let\@citex\NAT@citex
    \let\@biblabel\NAT@biblabel
    \let\@bibsetup\NAT@bibsetup
    \let\NAT@space\NAT@spacechar
    \let\NAT@penalty\@empty
    \renewcommand\NAT@idxtxt{\NAT@name\NAT@spacechar\NAT@open\NAT@date\NAT@close}%
    \def\natexlab##1{##1}%
  \fi}

%%%%% Hyperlinks
%% Define color for citations
\definecolor{bluecite}{HTML}{0875b7}

\ifthenelse{\equal{\@arttype}{Reprint} \OR \equal{\@arttype}{Book}}{
	\RequirePackage[unicode=true,
	bookmarksopen={true},
	pdffitwindow=true, 
	colorlinks=true, 
	linkcolor=black, 
	citecolor=black, 
	urlcolor=black, 
	hyperfootnotes=false, 
	pdfstartview={FitH},
	pdfpagemode=UseNone]{hyperref}
	}{
	\RequirePackage[unicode=true,
	bookmarksopen={true},
	pdffitwindow=true, 
	colorlinks=true, 
	linkcolor=bluecite, 
	citecolor=bluecite, 
	urlcolor=bluecite, 
	hyperfootnotes=false, 
	pdfstartview={FitH},
	pdfpagemode= UseNone]{hyperref}
} 

%% To have the possibility to change the urlcolor
\newcommand{\changeurlcolor}[1]{\hypersetup{urlcolor=#1}} 

%% Metadata
\newcommand{\org@maketitle}{}% LATEX-Check
\let\org@maketitle\maketitle
\def\maketitle{%
	\hypersetup{
		pdftitle={\@Title},
		pdfsubject={\@abstract},
		pdfkeywords={\@keyword},
		pdfauthor={\@AuthorNames}
		}%
	\org@maketitle
}

%%%% Footnotes
\RequirePackage[hang]{footmisc}
\setlength{\skip\footins}{1.2cm}
\setlength{\footnotemargin}{5mm}
\def\footnoterule{\kern-14\p@
\hrule \@width 2in \kern 11.6\p@}

%%%% URL
\RequirePackage{url}
\urlstyle{same}
\g@addto@macro{\UrlBreaks}{\UrlOrds} 

%%%% Widows & orphans
\clubpenalty=10000
\widowpenalty=10000
\displaywidowpenalty=10000

%%%% Front matter
\newcommand{\firstargument}{}
\newcommand{\Title}[1]{\gdef\@Title{#1}}%
\newcommand{\Author}[1]{\gdef\@Author{#1}}%
\def\@AuthorNames{}
\newcommand{\AuthorNames}[1]{\gdef\@AuthorNames{#1}}%
\newcommand{\firstpage}[1]{\gdef\@firstpage{#1}}
\newcommand{\doinum}[1]{\gdef\@doinum{#1}}

\doinum{10.3390/\@doiabbr\@pubvolume\num[minimum-integer-digits = 2]{\@issuenum}\num[minimum-integer-digits = 4]{\@articlenumber}}

\newcommand{\pubvolume}[1]{\gdef\@pubvolume{#1}}
\newcommand{\pubyear}[1]{\gdef\@pubyear{#1}}
\newcommand{\copyrightyear}[1]{\gdef\@copyrightyear{#1}}
\newcommand{\address}[2][]{\renewcommand{\firstargument}{#1}\gdef\@address{#2}}
\newcommand{\corresfirstargument}{}
\def\@corres{}
\newcommand{\corres}[2][]{\renewcommand{\corresfirstargument}{#1}\gdef\@corres{#2}} 
\def\@conference{}
\newcommand{\conference}[1]{\gdef\@conference{#1}}%
\def\@abstract{}
\renewcommand{\abstract}[1]{\gdef\@abstract{#1}}
\def\@externaleditor{}
\newcommand{\externaleditor}[1]{\gdef\@externaleditor{#1}}
\newcommand{\history}[1]{\gdef\@history{#1}} 
\def\@pacs{}
\newcommand{\PACS}[1]{\gdef\@pacs{#1}} 
\def\@msc{}
\newcommand{\MSC}[1]{\gdef\@msc{#1}} 
\def\@jel{}
\newcommand{\JEL}[1]{\gdef\@jel{#1}}
\def\@keyword{}
\newcommand{\keyword}[1]{\gdef\@keyword{#1}}
\def\@dataset{}
\newcommand{\dataset}[1]{\gdef\@dataset{#1}}
\def\@datasetlicense{}
\newcommand{\datasetlicense}[1]{\gdef\@datasetlicense{#1}}
\def\@featuredapplication{}
\newcommand{\featuredapplication}[1]{\gdef\@featuredapplication{#1}}
\def\@keycontribution{}
\newcommand{\keycontribution}[1]{\gdef\@keycontribution{#1}}


\def\@issuenum{}
\newcommand{\issuenum}[1]{\gdef\@issuenum{#1}}
\def\@updates{}
\newcommand{\updates}[1]{\gdef\@updates{#1}}

\def\@firstnote{}
\newcommand{\firstnote}[1]{\gdef\@firstnote{#1}}
\def\@secondnote{}
\newcommand{\secondnote}[1]{\gdef\@secondnote{#1}}%
\def\@thirdnote{}
\newcommand{\thirdnote}[1]{\gdef\@thirdnote{#1}}%
\def\@fourthnote{}
\newcommand{\fourthnote}[1]{\gdef\@fourthnote{#1}}%
\def\@fifthnote{}
\newcommand{\fifthnote}[1]{\gdef\@fifthnote{#1}}%
\def\@sixthnote{}
\newcommand{\sixthnote}[1]{\gdef\@sixthnote{#1}}%
\def\@seventhnote{}
\newcommand{\seventhnote}[1]{\gdef\@seventhnote{#1}}%
\def\@eighthnote{}
\newcommand{\eighthnote}[1]{\gdef\@eighthnote{#1}}%

\def\@simplesumm{}
\newcommand{\simplesumm}[1]{\gdef\@simplesumm{#1}}
\newcommand{\articlenumber}[1]{\gdef\@articlenumber{#1}}

\def\@externalbibliography{}
\newcommand{\externalbibliography}[1]{\gdef\@externalbibliography{#1}}

\def\@reftitle{}
\newcommand{\reftitle}[1]{\gdef\@reftitle{#1}}

%Reprint
\def\@originalarttype{}
\newcommand{\originalarttype}[1]{\gdef\@originalarttype{#1}}%


%% ORCID
% Make Orcid icon
\definecolor{lime}{HTML}{A6CE39}
\newcommand{\orcidicon}{%
	\begin{tikzpicture}
	\draw[lime, fill=lime] (0,0) 
		circle [radius=0.16] 
		node[white] {{\fontfamily{qag}\selectfont \tiny ID}};
	\draw[white, fill=white] (-0.0625,0.095) 
		circle [radius=0.007];
	\end{tikzpicture}
	\hspace{-2mm}
}

% Define link and button for each author
\foreach \x in {A, ..., Z}{%
\expandafter\xdef\csname orcid\x\endcsname{\noexpand\href{https://orcid.org/\csname orcidauthor\x\endcsname}{\noexpand\orcidicon}}
}

%%%% Journal name for the header
\newcommand{\journalname}{%
  \ifthenelse{\equal{\@journal}{test}}{Test}{}%
  \ifthenelse{\equal{\@journal}{notspecified}}{MDPI}{}%
  \ifthenelse{\equal{\@journal}{actuators}}{Actuators}{}%
  \ifthenelse{\equal{\@journal}{addictions}}{Addictions}{}%
  \ifthenelse{\equal{\@journal}{admsci}}{Adm. Sci.}{}%
  \ifthenelse{\equal{\@journal}{aerospace}}{Aerospace}{}%
  \ifthenelse{\equal{\@journal}{agriculture}}{Agriculture}{}%
  \ifthenelse{\equal{\@journal}{agronomy}}{Agronomy}{}%
  \ifthenelse{\equal{\@journal}{algorithms}}{Algorithms}{}%
  \ifthenelse{\equal{\@journal}{animals}}{Animals}{}%
  \ifthenelse{\equal{\@journal}{antibiotics}}{Antibiotics}{}%
  \ifthenelse{\equal{\@journal}{antibodies}}{Antibodies}{}%
  \ifthenelse{\equal{\@journal}{antioxidants}}{Antioxidants}{}%
  \ifthenelse{\equal{\@journal}{applsci}}{Appl. Sci.}{}%
  \ifthenelse{\equal{\@journal}{arts}}{Arts}{}%
  \ifthenelse{\equal{\@journal}{asi}}{Appl. Syst. Innov.}{}%
  \ifthenelse{\equal{\@journal}{atmosphere}}{Atmosphere}{}%
  \ifthenelse{\equal{\@journal}{atoms}}{Atoms}{}%
  \ifthenelse{\equal{\@journal}{axioms}}{Axioms}{}%
  \ifthenelse{\equal{\@journal}{batteries}}{Batteries}{}%
  \ifthenelse{\equal{\@journal}{bdcc}}{Big Data Cogn. Comput.}{}%
  \ifthenelse{\equal{\@journal}{behavsci}}{Behav. Sci.}{}%
  \ifthenelse{\equal{\@journal}{beverages}}{Beverages}{}%
  \ifthenelse{\equal{\@journal}{bioengineering}}{Bioengineering}{}%
  \ifthenelse{\equal{\@journal}{biology}}{Biology}{}%
  \ifthenelse{\equal{\@journal}{biomedicines}}{Biomedicines}{}%
  \ifthenelse{\equal{\@journal}{biomimetics}}{Biomimetics}{}%
  \ifthenelse{\equal{\@journal}{biomolecules}}{Biomolecules}{}%
  \ifthenelse{\equal{\@journal}{biosensors}}{Biosensors}{}%
  \ifthenelse{\equal{\@journal}{brainsci}}{Brain Sci.}{}%
  \ifthenelse{\equal{\@journal}{buildings}}{Buildings}{}%
  \ifthenelse{\equal{\@journal}{carbon}}{C}{}%
  \ifthenelse{\equal{\@journal}{cancers}}{Cancers}{}%
  \ifthenelse{\equal{\@journal}{catalysts}}{Catalysts}{}%
  \ifthenelse{\equal{\@journal}{cells}}{Cells}{}%
  \ifthenelse{\equal{\@journal}{ceramics}}{Ceramics}{}%
  \ifthenelse{\equal{\@journal}{challenges}}{Challenges}{}%
  \ifthenelse{\equal{\@journal}{chemengineering}}{ChemEngineering}{}%
  \ifthenelse{\equal{\@journal}{chemosensors}}{Chemosensors}{}%
  \ifthenelse{\equal{\@journal}{children}}{Children}{}%
  \ifthenelse{\equal{\@journal}{climate}}{Climate}{}%
  \ifthenelse{\equal{\@journal}{coatings}}{Coatings}{}%
  \ifthenelse{\equal{\@journal}{colloids}}{Colloids Interfaces}{}%
  \ifthenelse{\equal{\@journal}{computation}}{Computation}{}%
  \ifthenelse{\equal{\@journal}{computers}}{Computers}{}%
  \ifthenelse{\equal{\@journal}{condensedmatter}}{Condens. Matter}{}%
  \ifthenelse{\equal{\@journal}{cosmetics}}{Cosmetics}{}%
  \ifthenelse{\equal{\@journal}{cryptography}}{Cryptography}{}%
  \ifthenelse{\equal{\@journal}{crystals}}{Crystals}{}%
  \ifthenelse{\equal{\@journal}{cybersecurity}}{Cybersecurity}{}%
  \ifthenelse{\equal{\@journal}{data}}{Data}{}%
  \ifthenelse{\equal{\@journal}{dentistry}}{Dent. J.}{}%
  \ifthenelse{\equal{\@journal}{designs}}{Designs}{}%
  \ifthenelse{\equal{\@journal}{diagnostics}}{Diagnostics}{}%
  \ifthenelse{\equal{\@journal}{diseases}}{Diseases}{}%
  \ifthenelse{\equal{\@journal}{diversity}}{Diversity}{}%
  \ifthenelse{\equal{\@journal}{drones}}{Drones}{}%
  \ifthenelse{\equal{\@journal}{econometrics}}{Econometrics}{}%
  \ifthenelse{\equal{\@journal}{economies}}{Economies}{}%
  \ifthenelse{\equal{\@journal}{education}}{Educ. Sci.}{}%
  \ifthenelse{\equal{\@journal}{electrochem}}{Electrochem}{}%
  \ifthenelse{\equal{\@journal}{electrochemistry}}{Electrochemistry}{}%
  \ifthenelse{\equal{\@journal}{electronics}}{Electronics}{}%
  \ifthenelse{\equal{\@journal}{energies}}{Energies}{}%
  \ifthenelse{\equal{\@journal}{entropy}}{Entropy}{}%
  \ifthenelse{\equal{\@journal}{environments}}{Environments}{}%
  \ifthenelse{\equal{\@journal}{epigenomes}}{Epigenomes}{}%
  \ifthenelse{\equal{\@journal}{est}}{Electrochem. Sci. Technol.}{}%
  \ifthenelse{\equal{\@journal}{fermentation}}{Fermentation}{}%
  \ifthenelse{\equal{\@journal}{fibers}}{Fibers}{}%
  \ifthenelse{\equal{\@journal}{fire}}{Fire}{}%
  \ifthenelse{\equal{\@journal}{fishes}}{Fishes}{}%
  \ifthenelse{\equal{\@journal}{fluids}}{Fluids}{}%
  \ifthenelse{\equal{\@journal}{foods}}{Foods}{}%
  \ifthenelse{\equal{\@journal}{forecasting}}{Forecasting}{}%
  \ifthenelse{\equal{\@journal}{forests}}{Forests}{}%
  \ifthenelse{\equal{\@journal}{fractalfract}}{Fractal Fract.}{}%
  \ifthenelse{\equal{\@journal}{futureinternet}}{Future Internet}{}%
  \ifthenelse{\equal{\@journal}{galaxies}}{Galaxies}{}%
  \ifthenelse{\equal{\@journal}{games}}{Games}{}%
  \ifthenelse{\equal{\@journal}{gastrointestdisord}}{Gastrointest. Disord.}{}%
  \ifthenelse{\equal{\@journal}{gels}}{Gels}{}%
  \ifthenelse{\equal{\@journal}{genealogy}}{Genealogy}{}%
  \ifthenelse{\equal{\@journal}{genes}}{Genes}{}%
  \ifthenelse{\equal{\@journal}{geosciences}}{Geosciences}{}%
  \ifthenelse{\equal{\@journal}{geriatrics}}{Geriatrics}{}%
  \ifthenelse{\equal{\@journal}{hazardousmatters}}{Hazard. Matters}{}%
  \ifthenelse{\equal{\@journal}{healthcare}}{Healthcare}{}%
  \ifthenelse{\equal{\@journal}{heritage}}{Heritage}{}%
  \ifthenelse{\equal{\@journal}{highthroughput}}{High-Throughput}{}%
  \ifthenelse{\equal{\@journal}{horticulturae}}{Horticulturae}{}%
  \ifthenelse{\equal{\@journal}{humanities}}{Humanities}{}%
  \ifthenelse{\equal{\@journal}{hydrology}}{Hydrology}{}%
  \ifthenelse{\equal{\@journal}{informatics}}{Informatics}{}%
  \ifthenelse{\equal{\@journal}{information}}{Information}{}%
  \ifthenelse{\equal{\@journal}{infrastructures}}{Infrastructures}{}%
  \ifthenelse{\equal{\@journal}{inorganics}}{Inorganics}{}%
  \ifthenelse{\equal{\@journal}{insects}}{Insects}{}%
  \ifthenelse{\equal{\@journal}{instruments}}{Instruments}{}%
  \ifthenelse{\equal{\@journal}{ijerph}}{Int. J. Environ. Res. Public Health}{}%
  \ifthenelse{\equal{\@journal}{ijfs}}{Int. J. Financial Stud.}{}%
  \ifthenelse{\equal{\@journal}{ijms}}{Int. J. Mol. Sci.}{}%
  \ifthenelse{\equal{\@journal}{ijgi}}{ISPRS Int. J. Geo-Inf.}{}%
  \ifthenelse{\equal{\@journal}{ijtpp}}{Int. J. Turbomach. Propuls. Power}{}%
  \ifthenelse{\equal{\@journal}{inventions}}{Inventions}{}%
  \ifthenelse{\equal{\@journal}{jjournal}}{J}{}%
  \ifthenelse{\equal{\@journal}{jcdd}}{J. Cardiovasc. Dev. Dis.}{}%
  \ifthenelse{\equal{\@journal}{jcm}}{J. Clin. Med.}{}%
  \ifthenelse{\equal{\@journal}{jcs}}{J. Compos. Sci.}{}%
  \ifthenelse{\equal{\@journal}{jdb}}{J. Dev. Biol.}{}%
  \ifthenelse{\equal{\@journal}{jfb}}{J. Funct. Biomater.}{}%   
  \ifthenelse{\equal{\@journal}{jfmk}}{J. Funct. Morphol. Kinesiol.}{}%   
  \ifthenelse{\equal{\@journal}{jof}}{J. Fungi}{}%
  \ifthenelse{\equal{\@journal}{jimaging}}{J. Imaging}{}%
  \ifthenelse{\equal{\@journal}{jintelligence}}{J. Intell.}{}% 
  \ifthenelse{\equal{\@journal}{jlpea}}{J. Low Power Electron. Appl.}{}%
  \ifthenelse{\equal{\@journal}{jmmp}}{J. Manuf. Mater. Process.}{}%
  \ifthenelse{\equal{\@journal}{jmse}}{J. Mar. Sci. Eng.}{}%
  \ifthenelse{\equal{\@journal}{joitmc}}{J. Open Innov. Technol. Mark. Complex.}{}%
  \ifthenelse{\equal{\@journal}{jpm}}{J. Pers. Med.}{}%
  \ifthenelse{\equal{\@journal}{jrfm}}{J. Risk Financial Manag.}{}%
  \ifthenelse{\equal{\@journal}{jsan}}{J. Sens. Actuator Netw.}{}%
  \ifthenelse{\equal{\@journal}{land}}{Land}{}%
  \ifthenelse{\equal{\@journal}{languages}}{Languages}{}%
  \ifthenelse{\equal{\@journal}{laws}}{Laws}{}%
  \ifthenelse{\equal{\@journal}{life}}{Life}{}%
  \ifthenelse{\equal{\@journal}{literature}}{Literature}{}%
  \ifthenelse{\equal{\@journal}{logistics}}{Logistics}{}%
  \ifthenelse{\equal{\@journal}{lubricants}}{Lubricants}{}%
  \ifthenelse{\equal{\@journal}{machines}}{Machines}{}%
  \ifthenelse{\equal{\@journal}{magnetochemistry}}{Magnetochemistry}{}%
  \ifthenelse{\equal{\@journal}{make}}{Mach. Learn. Knowl. Extr.}{}%
  \ifthenelse{\equal{\@journal}{marinedrugs}}{Mar. Drugs}{}%
  \ifthenelse{\equal{\@journal}{materials}}{Materials}{}%
  \ifthenelse{\equal{\@journal}{mathematics}}{Mathematics}{}%
  \ifthenelse{\equal{\@journal}{mca}}{Math. Comput. Appl.}{}%
  \ifthenelse{\equal{\@journal}{medsci}}{Med. Sci.}{}% 
  \ifthenelse{\equal{\@journal}{medicines}}{Medicines}{}%
  \ifthenelse{\equal{\@journal}{membranes}}{Membranes}{}%
  \ifthenelse{\equal{\@journal}{metabolites}}{Metabolites}{}%
  \ifthenelse{\equal{\@journal}{metals}}{Metals}{}%
  \ifthenelse{\equal{\@journal}{microarrays}}{Microarrays}{}%
  \ifthenelse{\equal{\@journal}{micromachines}}{Micromachines}{}%
  \ifthenelse{\equal{\@journal}{microorganisms}}{Microorganisms}{}%
  \ifthenelse{\equal{\@journal}{minerals}}{Minerals}{}%
  \ifthenelse{\equal{\@journal}{modelling}}{Modelling}{}%
  \ifthenelse{\equal{\@journal}{molbank}}{Molbank}{}%
  \ifthenelse{\equal{\@journal}{molecules}}{Molecules}{}%
  \ifthenelse{\equal{\@journal}{mps}}{Methods and Protoc.}{}%
  \ifthenelse{\equal{\@journal}{mti}}{Multimodal Technologies and Interact.}{}%
  \ifthenelse{\equal{\@journal}{nanomaterials}}{Nanomaterials}{}%
  \ifthenelse{\equal{\@journal}{ncrna}}{Non-coding RNA}{}%
  \ifthenelse{\equal{\@journal}{neonatalscreening}}{Int. J. Neonatal Screen.}{}%
  \ifthenelse{\equal{\@journal}{neuroglia}}{Neuroglia}{}%
  \ifthenelse{\equal{\@journal}{nitrogen}}{Nitrogen}{}%
  \ifthenelse{\equal{\@journal}{nutrients}}{Nutrients}{}%
  \ifthenelse{\equal{\@journal}{ohbm}}{J. Otorhinolaryngol. Hear. Balance Med.}{}%
  \ifthenelse{\equal{\@journal}{particles}}{Particles}{}%
  \ifthenelse{\equal{\@journal}{pathogens}}{Pathogens}{}%
  \ifthenelse{\equal{\@journal}{pharmaceuticals}}{Pharmaceuticals}{}%
  \ifthenelse{\equal{\@journal}{pharmaceutics}}{Pharmaceutics}{}%
  \ifthenelse{\equal{\@journal}{pharmacy}}{Pharmacy}{}%
  \ifthenelse{\equal{\@journal}{philosophies}}{Philosophies}{}%
  \ifthenelse{\equal{\@journal}{photonics}}{Photonics}{}%
  \ifthenelse{\equal{\@journal}{plants}}{Plants}{}%
  \ifthenelse{\equal{\@journal}{plasma}}{Plasma}{}%
  \ifthenelse{\equal{\@journal}{polymers}}{Polymers}{}%
  \ifthenelse{\equal{\@journal}{polysaccharides}}{Polysaccharides}{}%
  \ifthenelse{\equal{\@journal}{proceedings}}{Proceedings}{}%
  \ifthenelse{\equal{\@journal}{processes}}{Processes}{}%
  \ifthenelse{\equal{\@journal}{proteomes}}{Proteomes}{}%
  \ifthenelse{\equal{\@journal}{publications}}{Publications}{}%
  \ifthenelse{\equal{\@journal}{quaternary}}{Quaternary}{}%
  \ifthenelse{\equal{\@journal}{qubs}}{Quantum Beam Sci.}{}%
  \ifthenelse{\equal{\@journal}{reactions}}{Reactions}{}%
  \ifthenelse{\equal{\@journal}{recycling}}{Recycling}{}%
  \ifthenelse{\equal{\@journal}{religions}}{Religions}{}%
  \ifthenelse{\equal{\@journal}{remotesensing}}{Remote Sens.}{}%
  \ifthenelse{\equal{\@journal}{reports}}{Reports}{}%
  \ifthenelse{\equal{\@journal}{resources}}{Resources}{}%
  \ifthenelse{\equal{\@journal}{risks}}{Risks}{}%
  \ifthenelse{\equal{\@journal}{robotics}}{Robotics}{}%
  \ifthenelse{\equal{\@journal}{safety}}{Safety}{}%
  \ifthenelse{\equal{\@journal}{scipharm}}{Sci. Pharm.}{}%
  \ifthenelse{\equal{\@journal}{sensors}}{Sensors}{}%
  \ifthenelse{\equal{\@journal}{separations}}{Separations}{}%
  \ifthenelse{\equal{\@journal}{sexes}}{Sexes}{}%
  \ifthenelse{\equal{\@journal}{sinusitis}}{Sinusitis}{}%
  \ifthenelse{\equal{\@journal}{socsci}}{Soc. Sci.}{}%
  \ifthenelse{\equal{\@journal}{societies}}{Societies}{}%
  \ifthenelse{\equal{\@journal}{soilsystems}}{Soil. Syst.}{}%
  \ifthenelse{\equal{\@journal}{sports}}{Sports}{}%
  \ifthenelse{\equal{\@journal}{standards}}{Standards}{}%
  \ifthenelse{\equal{\@journal}{stats}}{Stats}{}%
  \ifthenelse{\equal{\@journal}{surfaces}}{Surfaces}{}%
  \ifthenelse{\equal{\@journal}{surgeries}}{Surgeries}{}%
  \ifthenelse{\equal{\@journal}{sustainability}}{Sustainability}{}%
  \ifthenelse{\equal{\@journal}{symmetry}}{Symmetry}{}%
  \ifthenelse{\equal{\@journal}{systems}}{Systems}{}%
  \ifthenelse{\equal{\@journal}{technologies}}{Technologies}{}%
  \ifthenelse{\equal{\@journal}{toxics}}{Toxics}{}%
  \ifthenelse{\equal{\@journal}{toxins}}{Toxins}{}%
  \ifthenelse{\equal{\@journal}{tropicalmed}}{Trop. Med. Infect. Dis.}{}%
  \ifthenelse{\equal{\@journal}{universe}}{Universe}{}%
  \ifthenelse{\equal{\@journal}{urbansci}}{Urban Sci.}{}%
  \ifthenelse{\equal{\@journal}{vaccines}}{Vaccines}{}%
  \ifthenelse{\equal{\@journal}{vehicles}}{Vehicles}{}%
  \ifthenelse{\equal{\@journal}{vetsci}}{Vet. Sci.}{}%
  \ifthenelse{\equal{\@journal}{vibration}}{Vibration}{}%
  \ifthenelse{\equal{\@journal}{viruses}}{Viruses}{}%
  \ifthenelse{\equal{\@journal}{vision}}{Vision}{}%
  \ifthenelse{\equal{\@journal}{water}}{Water}{}%
  \ifthenelse{\equal{\@journal}{wem}}{Wildl. Ecol. Manag.}{}%
  }
    
%%%% Header and footer on first page
%% The plain page style needs to be redefined because with \maketitle in the article class, LaTeX applies the the plain page style automatically to the first page.
\ifthenelse{\equal{\@journal}{preprints} %
		\OR \equal{\@arttype}{Reprint} %
		\OR \equal{\@arttype}{Book}}{%
	\fancypagestyle{plain}{%
		\fancyhf{}
		\ifthenelse{\equal{\@arttype}{Reprint} %
			\OR \equal{\@arttype}{Book}}{
        			\fancyfoot[C]{%
        				\footnotesize%
        				\thepage}
       	 		\ifthenelse{\equal{\@arttype}{Book}}{}{%
        				\fancyfoot[L]{%
        					\footnotesize%
        					{\em \journalname}\ %
        					{\bfseries {\@pubyear}}, %
        					{\em \@pubvolume}, %
        					\ifthenelse{\equal{\@articlenumber}{\@empty}}{%
        						\@firstpage --\pageref*{LastPage}
        						}{\@articlenumber}%
        					}
        				\fancyfoot[R]{%
        	 				\footnotesize%
        					{\changeurlcolor{black}%
        					\href{http://www.mdpi.com/journal/\@journal}%
        					{www.mdpi.com/journal/\@journal}}%
        					}
        				}
     	   		}		
		}
	}{%
	\ifthenelse{\equal{\@arttype}{Supfile}}{
		\fancypagestyle{plain}{
			\fancyhf{}
			\fancyhead[R]{
				\footnotesize %
				S\thepage{} of S\pageref*{LastPage}%
				}%
			\fancyhead[L]{
				\footnotesize %
				\ifthenelse{\equal{\@status}{submit}}{%
					Version {\@ \today} submitted to {\em\journalname}%
					}{%
					{\em \journalname} %
					{\bfseries \@pubyear}, %
					{\em \@pubvolume}, %
					\ifthenelse{\equal{\@articlenumber}{\@empty}}{%
						\@firstpage --\pageref*{LastPage}%
						}{%
						\@articlenumber%
						}%
					; doi:{\changeurlcolor{black}%
        					\href{http://dx.doi.org/\@doinum}%
        					{\@doinum}}%
					}%
				}%
			}%
		}{
		\fancypagestyle{plain}{
			\fancyhf{}
			\fancyfoot[L]{
				\footnotesize%
 	 				\ifthenelse{\equal{\@status}{submit}}{%
					Submitted to {\em\journalname}, %
					pages \thepage \ -- \color{black}{\pageref*{LastPage}}%
					}{
					{\em \journalname}\ %
					{\bfseries \@pubyear}, %
					{\em \@pubvolume}, %
					\ifthenelse{\equal{\@articlenumber}{\@empty}}{%
						\@firstpage --\pageref*{LastPage}%
						}{%
						\@articlenumber%
						}%
						; doi:{\changeurlcolor{black}%
        					\href{http://dx.doi.org/\@doinum}%
        					{\@doinum}}%
					}%
				}%
			\fancyfoot[R]{
 				\footnotesize%
				{\changeurlcolor{black}%
				\href{http://www.mdpi.com/journal/\@journal}%
				{www.mdpi.com/journal/\@journal}}%
				}%
			\fancyhead{}
		 	\renewcommand{\headrulewidth}{0.0pt}%
			}
		}%
	}%	

%%%% Maketitle part 1: Logo, Arttype, Title, Author
\renewcommand{\@maketitle}{
	\begin{flushleft}
	\ifthenelse{\equal{\@arttype}{Supfile}}{%
		\fontsize{18}{18}\selectfont
		\raggedright
		\noindent\textbf{Supplementary Materials: \@Title}%
		\par
		\vspace{12pt}
		\fontsize{10}{10}\selectfont
		\noindent\boldmath\bfseries{\@Author}
		}{%
		\ifthenelse{\equal{\@arttype}{Book}}{}{%
			\vspace*{-1.75cm}
			}
		{%0
		\ifthenelse{\equal{\@journal}{preprints}
			\OR \equal{\@arttype}{Book}}{}{%
				\ifthenelse{\equal{\@status}{submit}}{%	
					\hfill \href{http://www.mdpi.com}{%
					\includegraphics[height=1cm]{logo-mdpi}}\vspace{0.5cm}%
					}{
					\href{http://www.mdpi.com/journal/\@journal}{
					\includegraphics[height=1.2cm]{\@journal-logo}}%
					\hfill
					\ifthenelse{\equal{\@journal}{proceedings}}{
						\href{http://www.mdpi.com/journal/\@journal}{
						\includegraphics[height=1.2cm]{logo-conference}
						\hfill}
						}{}
					\ifthenelse{\equal{\@journal}{scipharm}}{%
						\href{http://www.mdpi.com}{\includegraphics[height=1cm]{logo-mdpi-scipharm}}%
						}{%
						\href{http://www.mdpi.com}{\includegraphics[height=1cm]{logo-mdpi}}%
						}%
					}%
			}%
		\par
		}%0
		{%1
    		\vspace{14pt}
    		\fontsize{10}{10}\selectfont
		\ifthenelse{\equal{\@arttype}{Book}}{}{
			\ifthenelse{\equal{\@arttype}{Reprint}}{%
				\textit{\ifthenelse{\equal{\@originalarttype}{\@empty}}{Article}{\@originalarttype}}%
				}{%
				\textit{\@arttype}%
				}%
			}%	
 	   	\par%
    		}%1
    		{%2
   	 	\vspace{-1pt}
  	  	\fontsize{18}{18}\selectfont
   	 	\boldmath\bfseries{\@Title}
   	 	\par
   	 	\vspace{15pt}
   	 	}%2
   		{%3
    		\boldmath\bfseries{\@Author}
    		\par
    		\vspace{-4pt}
    		}%3
		}
	\end{flushleft}%
	}

% Commands for hanging indent
\newcommand{\dist}{1.7em}
\newcommand{\hang}{\hangafter=1\hangindent=\dist\noindent}

%%%% Maketitle part 2
\newcommand{\maketitlen}{ 
\ifthenelse{\equal{\@arttype}{Book}}{\vspace{12pt}}{
	\begin{flushleft}
	\begin{spacing}{1.35}
	\leftskip0.2cm
	\fontsize{9}{9}\selectfont
	{%
	\ifthenelse{\equal{\firstargument}{1}}{}{%
	\hang}\@address
	\par
	}%
	{%
	\ifthenelse{\equal{\@authornum}{author}}{}{%
	\ifthenelse{\equal{\@corres}{\@empty}}{}{%
	\hang\textbf{*} \tabto{\dist} \@corres}
	\par
	}
	}%
	{%
	\ifthenelse{\equal{\@conference}{\@empty}}{}{%
	\hang$\dagger$ \tabto{\dist} This paper is an extended version of our paper published in\space \@conference.}
	\par
	}%
	{%
	\ifthenelse{\equal{\@firstnote}{\@empty}}{}{%
	\hang\ifthenelse{\equal{\@conference}{\@empty}}{$\dagger$}{$\ddagger$} \tabto{\dist} \@firstnote}	
	\par
	}%
	{%
	\ifthenelse{\equal{\@secondnote}{\@empty}}{}{%
	\hang \ifthenelse{\equal{\@conference}{\@empty}}{$\ddagger$}{\S} \tabto{\dist} \@secondnote}
	\par
	}%
	{%
	\ifthenelse{\equal{\@thirdnote}{\@empty}}{}{%
	\hang \ifthenelse{\equal{\@conference}{\@empty}}{\S}{$\|$} \tabto{\dist} \@thirdnote}
	\par
	}%
	{%
	\ifthenelse{\equal{\@fourthnote}{\@empty}}{}{%
	\hang \ifthenelse{\equal{\@conference}{\@empty}}{$\|$}{\P} \tabto{\dist} \@fourthnote}
	\par
	}%
	{%
	\ifthenelse{\equal{\@fifthnote}{\@empty}}{}{%
	\hang \ifthenelse{\equal{\@conference}{\@empty}}{\P}{**} \tabto{\dist} \@fifthnote}
	\par
	}%
	{%
	\ifthenelse{\equal{\@sixthnote}{\@empty}}{}{%
	\hang \ifthenelse{\equal{\@conference}{\@empty}}{**}{$\dagger\dagger$} \tabto{\dist} \@sixthnote}
	\par
	}%
	{%
	\ifthenelse{\equal{\@seventhnote}{\@empty}}{}{%
	\hang \ifthenelse{\equal{\@conference}{\@empty}}{$\dagger\dagger$}{$\ddagger\ddagger$} \tabto{\dist} \@seventhnote}
	\par
	}%
	{%
	\ifthenelse{\equal{\@eighthnote}{\@empty}}{}{%
	\hang \ifthenelse{\equal{\@conference}{\@empty}}{$\ddagger\ddagger$}{***} \tabto{\dist} \@eighthnote}
	\par
	}%
	{%
	\vspace{6pt}
	\ifthenelse{\equal{\@externaleditor}{\@empty}}{}{\@externaleditor}
	\par
	}%
	{%
	\ifthenelse{\equal{\@status}{submit}}{Version {\@ \today} submitted to \journalname}{\@history\hfill \ifthenelse{\equal{\@updates}{\@empty}}{}{\href{http://www.mdpi.com/\@ISSN/\@pubvolume/\@issuenum/\@articlenumber?type=check_update&version=1}{\includegraphics[height=.6cm]{logo-updates}}}}
	\par
	\vspace{-4pt}%
	}%
	\end{spacing}
	\end{flushleft}
}
}

%%%% Abstract, keywords, journal data, PACS, MSC, JEL
\newcommand{\abstractkeywords}{
\vspace{-8pt}
{% For journal Applied Sciences:
\ifthenelse{\equal{\@featuredapplication}{\@empty}}{}{
\begingroup
\leftskip0.2cm
\noindent\textbf{Featured Application:\space\@featuredapplication}
\vspace{12pt}
\par
\endgroup}
}%
{%10
\begingroup
\leftskip0.2cm 
\ifthenelse{\equal{\@simplesumm}{\@empty}}{}{
\noindent\textbf{Simple Summary:\space}\@simplesumm
\vspace{12pt}
\par
}
\ifthenelse{\equal{\@abstract}{\@empty}}{}{
\noindent\textbf{Abstract:\space}\@abstract
\vspace{12pt}
\par
}
\endgroup
}%10
{% For journal Data:
\ifthenelse{\equal{\@dataset}{\@empty}}{}{
\begingroup
\leftskip0.2cm
\noindent\textbf{Dataset:\space}\@dataset
\vspace{12pt}
\par
\endgroup}
}%
{%For journal Data:
\ifthenelse{\equal{\@datasetlicense}{\@empty}}{}{
\begingroup
\leftskip0.2cm
\noindent\textbf{Dataset License:\space}\@datasetlicense
\vspace{12pt}
\par
\endgroup}
}%
{%11
\begingroup
\leftskip0.2cm
\ifthenelse{\equal{\@keyword}{\@empty}}{}{
\noindent\textbf{Keywords:\space}\@keyword
\vspace{12pt}
\par
}
\endgroup
}%11
{%For journal Toxins:
\begingroup
\leftskip0.2cm
\ifthenelse{\equal{\@keycontribution}{\@empty}}{}{
\noindent\textbf{Key Contribution:\space}\@keycontribution
\vspace{12pt}
\par
}
\endgroup
}%11
{%12
\ifthenelse{\equal{\@pacs}{\@empty}}{}{
\begingroup
\leftskip0.2cm
\noindent\textbf{PACS:\space}\@pacs
\vspace{12pt}
\par
\endgroup}
}%12
{%13
\ifthenelse{\equal{\@msc}{\@empty}}{}{
\begingroup
\leftskip0.2cm
\noindent\textbf{MSC:\space}\@msc
\vspace{12pt}
\par
\endgroup}
}%13
{%14
\ifthenelse{\equal{\@jel}{\@empty}}{}{
\begingroup
\leftskip0.2cm
\noindent\textbf{JEL Classification:\space}\@jel
\vspace{12pt}
\par
\endgroup}
}%14
\vspace{4pt}
\hrule
\vspace{12pt}
}


%%%% Print maketitle and abstractkeywords
\ifthenelse{\equal{\@arttype}{Supfile}}{
	\AfterEndPreamble{
	\maketitle
	\let\maketitle\relax
	\ifthenelse{\equal{\@status}{submit}}{\linenumbers}{}
	}%
	}{
	\AfterEndPreamble{
	\maketitle
	\let\maketitle\relax
	\maketitlen
	\let\maketitlen\relax
	\ifthenelse{\equal{\@status}{submit}}{\linenumbers}{}
	\abstractkeywords
	}%
	}
\AtBeginDocument{
	\DeclareSymbolFont{AMSb}{U}{msb}{m}{n}
	\DeclareSymbolFontAlphabet{\mathbb}{AMSb}
	}

%%%% Font size in Tables
\AtEndPreamble{
	\def\@tablesize{}
	\newcommand{\tablesize}[1]{\gdef\@tablesize{#1}}
	\let\oldtabular\tabular
	\renewcommand{\tabular}{\ifthenelse{\equal{\@tablesize}{\@empty}}{\small}{\@tablesize}\oldtabular}
}

%%%% Section headings
\setcounter{secnumdepth}{4} %i.e., section numbering depth, which defaults to 3 in the article class. To get paragraphs numbered and counted, increase the default value of secnumdepth to 4

\titleformat {\section} [block] {\raggedright \fontsize{10}{10}\selectfont\bfseries} {\thesection.\space} {0pt} {}
\titlespacing {\section} {0pt} {12pt} {6pt}

\titleformat {\subsection} [block] {\raggedright \fontsize{10}{10}\selectfont\itshape} {\thesubsection.\space} {0pt} {}
\titlespacing {\subsection} {0pt} {12pt} {6pt}

\titleformat {\subsubsection} [block] {\raggedright \fontsize{10}{10}\selectfont} {\thesubsubsection.\space} {0pt} {}
\titlespacing {\subsubsection} {0pt} {12pt} {6pt}

\titleformat {\paragraph} [block] {\raggedright \fontsize{10}{10}\selectfont} {} {0pt} {}
\titlespacing {\paragraph} {0pt} {12pt} {6pt}

%%%% Special section title style for back matter
\newcommand{\supplementary}[1]{
\par\vspace{6pt}\noindent{\fontsize{9}{9}\selectfont\textbf{Supplementary Materials:} {#1}\par}}

\newcommand{\acknowledgments}[1]{
\vspace{6pt}\noindent{\fontsize{9}{9}\selectfont\textbf{Acknowledgments:} {#1}\par}}

\newcommand{\authorcontributions}[1]{%
\vspace{6pt}\noindent{\fontsize{9}{9}\selectfont\textbf{Author Contributions:} {#1}\par}}

\newcommand{\conflictsofinterest}[1]{%
\vspace{6pt}\noindent{\fontsize{9}{9}\selectfont\textbf{Conflicts of Interest:} {#1}\par}}

\newcommand{\conflictofinterest}[1]{% Backwards compatibility for book prodcution
\vspace{6pt}\noindent{\fontsize{9}{9}\selectfont\textbf{Conflicts of Interest:} {#1}\par}}

\newcommand{\conflictofinterests}[1]{% Backwards compatibility for book prodcution
\vspace{6pt}\noindent{\fontsize{9}{9}\selectfont\textbf{Conflicts of Interest:} {#1}\par}}

\newcommand{\sampleavailability}[1]{%
\vspace{12pt}\noindent{\fontsize{9}{9}\selectfont\textbf{Sample Availability:} {#1}\par}}

\newcommand{\abbreviations}[1]{%
\vspace{12pt}\noindent{\selectfont\textbf{Abbreviations}\par\vspace{6pt}\noindent {\fontsize{9}{9}\selectfont #1}\par}}

%%%%% Defines the appendix
\def\@appendixtitles{}
\newcommand{\appendixtitles}[1]{\gdef\@appendixtitles{#1}}

\def\@appendixsections{}
\newcommand{\appendixsections}[1]{\gdef\@appendixsections{#1}}

\renewcommand{\appendix}{%
\setcounter{section}{0}%
\setcounter{subsection}{0}%
\setcounter{subsubsection}{0}%
%
\gdef\thesection{\@Alph\c@section}%
\gdef\thesubsection{\@Alph\c@section.\@arabic\c@subsection}%
%	
\titleformat {\section} [block] {\raggedright \fontsize{10}{10}\selectfont\bfseries} {%
	\ifthenelse{\equal{\@appendixtitles}{yes}}{%
		\ifthenelse{\equal{\@appendixsections}{multiple}}{%
			\appendixname~\thesection.%
			}{%
			\appendixname.}
			}{%
		\ifthenelse{\equal{\@appendixsections}{multiple}}{%
			\appendixname~\thesection~%
			}{%
			\appendixname~}
		}
	} {0pt} {}
\titlespacing {\section} {0pt} {12pt} {6pt}
%
\titleformat {\subsection} [block] {\raggedright \fontsize{10}{10}\selectfont\itshape} {%
	\ifthenelse{\equal{\@appendixtitles}{yes}}{%
		\appendixname~\thesubsection.%
		}{%
		\appendixname~\thesubsection%
		}
	} {0pt} {}
\titlespacing {\subsection} {0pt} {12pt} {6pt}
%
\titleformat {\subsubsection} [block] {\raggedright \fontsize{10}{10}\selectfont} {%
	\ifthenelse{\equal{\@appendixtitles}{yes}}{%
		\appendixname~\thesubsubsection.%
		}{%
		\appendixname~\thesubsubsection%
		}
	} {0pt} {}
\titlespacing {\subsubsection} {0pt} {12pt} {6pt}
%
\gdef\theHsection{\@Alph\c@section.}% for hyperref
\gdef\theHsubsection{\@Alph\c@section.\@arabic\c@subsection}% for hyperref
\csname appendixmore\endcsname
\renewcommand{\thefigure}{A\arabic{figure}}
\setcounter{figure}{0}
\renewcommand{\thetable}{A\arabic{table}}
\setcounter{table}{0}
\renewcommand{\thescheme}{A\arabic{scheme}}
\setcounter{scheme}{0}
\renewcommand{\thechart}{A\arabic{chart}}
\setcounter{chart}{0}
\renewcommand{\theboxenv}{A\arabic{boxenv}}
\setcounter{boxenv}{0}
\renewcommand{\theequation}{A\arabic{equation}}
\setcounter{equation}{0}
\renewcommand{\thetheorem}{A\arabic{theorem}}
\setcounter{theorem}{0}
\renewcommand{\thelemma}{A\arabic{lemma}}
\setcounter{lemma}{0}
\renewcommand{\thecorollary}{A\arabic{corollary}}
\setcounter{corollary}{0}
\renewcommand{\theproposition}{A\arabic{proposition}} 
\setcounter{proposition}{0} 
\renewcommand{\thecharacterization}{A\arabic{characterization}}
\setcounter{characterization}{0} 
\renewcommand{\theproperty}{A\arabic{property}}
\setcounter{property}{0} 
\renewcommand{\theproblem}{A\arabic{problem}}
\setcounter{problem}{0} 
\renewcommand{\theexample}{A\arabic{example}}
\setcounter{example}{0} 
\renewcommand{\theexamplesanddefinitions}{A\arabic{examplesanddefinitions}}
\setcounter{examplesanddefinitions}{0} 
\renewcommand{\theremark}{A\arabic{remark}}
\setcounter{remark}{0} 
\renewcommand{\thedefinition}{A\arabic{definition}}
\setcounter{definition}{0} 
\renewcommand{\thehypothesis}{A\arabic{hypothesis}}
\setcounter{hypothesis}{0}
\renewcommand{\thenotation}{A\arabic{notation}}
\setcounter{notation}{0}
}

%%%% Layout
\ifthenelse{\equal{\@arttype}{Book}}{%%
	\RequirePackage[left=2.05cm,
					right=2.05cm,
					top=2.05cm,
					bottom=2.05cm,
					paperwidth=170mm,
					paperheight=244mm,
					includefoot]{geometry}
	}{
	\RequirePackage[left=2.7cm,
				right=2.7cm,
				top=1.8cm,
				bottom=1.5cm,
				includehead,
				includefoot]{geometry}
	}

\linespread{1.13} 
\setlength{\parindent}{0.75cm}

%%%% Figures and tables
\newfloat{boxenv}{H}{lop}
\floatname{boxenv}{Box}

\newfloat{chart}{H}{lop}
\floatname{chart}{Chart}

\newfloat{scheme}{H}{lop}
\floatname{scheme}{Scheme}

\floatstyle{plain}
\restylefloat{figure}

\floatstyle{plaintop}
\restylefloat{table}

\floatstyle{plaintop}
\restylefloat{boxenv}

\floatstyle{plain}
\restylefloat{scheme}

\RequirePackage{caption} 
\captionsetup[figure]{position=bottom, margin=0.75cm, labelfont={bf, small, stretch=1.17}, labelsep=period, textfont={small, stretch=1.17}, aboveskip=6pt, belowskip=12pt, justification=justified}

\captionsetup[scheme]{position=bottom, margin=0.75cm, labelfont={bf, small, stretch=1.17}, labelsep=period, textfont={small, stretch=1.17}, aboveskip=6pt, belowskip=12pt, justification=justified}

\captionsetup[chart]{position=bottom, margin=0.75cm, labelfont={bf, small, stretch=1.17}, labelsep=period, textfont={small, stretch=1.17}, aboveskip=6pt, belowskip=12pt, justification=justified}

\captionsetup[table]{position=top, margin=0.75cm, labelfont={bf, small, stretch=1.17}, labelsep=period, textfont={small, stretch=1.17}, aboveskip=6pt, belowskip=12pt, justification=justified}

\captionsetup[boxenv]{position=top, margin=0.75cm, labelfont={bf, small, stretch=1.17}, labelsep=period, textfont={small, stretch=1.17}, aboveskip=6pt, belowskip=12pt, justification=justified}

%% For table footnotes
\newsavebox{\@justcentbox}
\newcommand{\justifyorcenter}[1]{
\sbox \@justcentbox{#1}
\ifdim \wd \@justcentbox >\hsize #1
\else \centerline{#1} \fi
}

%%%% Bullet lists
\newlength{\wideitemsep}
\setlength{\wideitemsep}{.5\itemsep}
\addtolength{\wideitemsep}{-7pt}
\let\olditem\item
\renewcommand{\item}{\setlength{\itemsep}{\wideitemsep}\olditem}

%%%% Quote environment
\patchcmd{\quote}{\rightmargin}{\leftmargin 0.75cm \rightmargin}{}{}

%%%% Supplementary file
\ifthenelse{\equal{\@arttype}{Supfile}}{
	\renewcommand{\thefigure}{S\arabic{figure}}%
	\renewcommand{\thetable}{S\arabic{table}}%
	}{}%

%% Link to supplementary material: www.mdpi.com/ISSN-number/volume-number/issue-number/article-number	
\newcommand{\linksupplementary}{\url{http://www.mdpi.com/\@ISSN/\@pubvolume/\@issuenum/\@articlenumber}}

%%%% Header and footer (all pages except the first)
\renewcommand\headrule{} %% set line (from fancyhdr) in header to nothing
\pagestyle{fancy}
\lhead{
	\ifthenelse{\equal{\@journal}{preprints}%
	\OR \equal{\@arttype}{Book}}{%
		}{%
		\fontsize{8}{8}\selectfont%
		\ifthenelse{\equal{\@status}{submit}}{%
			Version {\@ \today} submitted to {\em \journalname}%
			}{%
			\ifthenelse{\equal{\@arttype}{Supfile}}{%
			{\em \journalname} {\bfseries \@pubyear}, {\em \@pubvolume}, %
			\ifthenelse{\equal{\@articlenumber}{}}{%
			\@firstpage --\pageref*{LastPage}%
			}{\@articlenumber}%
			; doi:{\changeurlcolor{black}%
        					\href{http://dx.doi.org/\@doinum}%
        					{\@doinum}}%
			}{%
			{\em\journalname\ }{\bfseries\@pubyear}, {\em \@pubvolume}, %
			\ifthenelse{\equal{\@articlenumber}{}}{%
			\@firstpage --\pageref*{LastPage}%
			}{%
			\@articlenumber}%
			}%
			}%
		}%
	}
\rhead{
\ifthenelse{\equal{\@arttype}{Reprint} \OR \equal{\@arttype}{Book}}{}{
%	\ifthenelse{\equal{\@journal}{recycling}
%		\OR \equal{\@journal}{philosophies}
%		\OR \equal{\@journal}{sinusitis}
%		\OR \equal{\@journal}{fishes}
%		\OR \equal{\@journal}{scipharm}
%		\OR \equal{\@journal}{jfmk}}{
%			\ifthenelse{\equal{\@arttype}{Supfile}}{
%				\fontsize{8}{8}\selectfont S\thepage{}
%				}{
%				\ifthenelse{\equal{\@status}{submit}}{
%					\fontsize{8}{8}\selectfont\thepage
%					}{
%					\fontsize{8}{8}\selectfont\thepage{}%
%					}%
%				}%
%			}{
			\ifthenelse{\equal{\@arttype}{Supfile}}{
				\fontsize{8}{8}\selectfont S\thepage{} of S\pageref*{LastPage}%
				}{
				\fontsize{8}{8}\selectfont\thepage{} of \pageref*{LastPage}%
				}%
%			}%
		}%
	}

\cfoot{
	\ifthenelse{\equal{\@arttype}{Reprint}
		\OR \equal{\@arttype}{Book}}{%
		\fontsize{8}{8}\selectfont\thepage
		}{}
	}


%%%% Bibliography
\renewcommand\bibname{References} % Backwards compatibility for book production
\renewcommand\@biblabel[1]{#1.\hfill}
\def\thebibliography#1{
\linespread{1.44} 
\section*{\@reftitle}
\addcontentsline{toc}{section}{References}
\fontsize{9}{9}\selectfont
\list{{\arabic{enumi}}}{\def\makelabel##1{\hss{##1}}
\topsep=0\p@
\parsep=5\p@
\partopsep=0\p@
\itemsep=0\p@
\labelsep=1.5mm
\ifthenelse{\equal{\@journal}{admsci}
\OR \equal{\@journal}{arts}
\OR \equal{\@journal}{econometrics}
\OR \equal{\@journal}{economies}
\OR \equal{\@journal}{genealogy}
\OR \equal{\@journal}{humanities}
\OR \equal{\@journal}{ijfs}
\OR \equal{\@journal}{jrfm}
\OR \equal{\@journal}{laws}
\OR \equal{\@journal}{religions}
\OR \equal{\@journal}{risks}
\OR \equal{\@journal}{socsci}}{%
	\ifthenelse{\equal{\@externalbibliography}{\@empty}}{%
		\itemindent=-7.7mm
		}{%
		\itemindent=-3.3mm}%
		}{%
	\itemindent=0\p@}
\settowidth\labelwidth{\footnotesize[#1]}%
\leftmargin\labelwidth
\advance\leftmargin\labelsep
%\advance\leftmargin -\itemindent
\usecounter{enumi}}
%\def\newblock{\ }
%\sloppy\clubpenalty4000\widowpenalty4000
%\sfcode`\.=1000\relax
}
\let\endthebibliography=\endlist

%%%% Copyright info
\newcommand{\cright}{%
        \ifthenelse{\equal{\@arttype}{Supfile} \OR \equal{\@journal}{preprints}}{%
		}{%
		\vspace{12pt}
		\noindent
		\linespread{1.44} 
		\fontsize{9}{9}\selectfont 
		\ifthenelse{\equal{\@status}{submit}}{
			\noindent \copyright{} {\@ \the\year} by the \@authornum. %
			Submitted to {\em \journalname} for %
			possible open access publication %
			under the terms and conditions of the Creative Commons Attribution %
			\ifthenelse{\equal{\@journal}{ijtpp}}{NonCommercial NoDerivatives (CC BY-NC-ND)}{(CC BY)} %
			license %
			\ifthenelse{\equal{\@journal}{ijtpp}}{
			(\changeurlcolor{black}%
			\href{https://creativecommons.org/licenses/by-nc-nd/4.0/.}%
			{https://creativecommons.org/licenses/by-nc-nd/4.0/}).%
			}{%
			(\changeurlcolor{black}%
			\href{http://creativecommons.org/licenses/by/4.0/.}%
			{http://creativecommons.org/licenses/by/4.0/}).}
			}{
			\begin{minipage}{.2\textwidth}
				\hspace{-1.2mm}%
				\vspace{2mm}%
				\href{http://creativecommons.org/}{%
				\ifthenelse{\equal{\@journal}{ijtpp}}{%
					\includegraphics[width=0.94\textwidth]{logo-ccby-nc-nd}%
					}{%
					\includegraphics[width=0.94\textwidth]{logo-ccby}
					}
				}
			\end{minipage}%
			\begin{minipage}{.79\textwidth}
				\copyright \ {\@copyrightyear} by the \@authornum. %
				Licensee MDPI, Basel, Switzerland. %
				This article is an open access article %
				distributed under the terms and conditions %
				of the Creative Commons Attribution %
				\ifthenelse{\equal{\@journal}{ijtpp}}{NonCommercial NoDerivatives (CC BY-NC-ND)}{(CC BY)} %
				license %
				\ifthenelse{\equal{\@journal}{ijtpp}}{
				(\changeurlcolor{black}%
				\href{https://creativecommons.org/licenses/by-nc-nd/4.0/.}%
				{https://creativecommons.org/licenses/by-nc-nd/4.0/}).%
				}{%
				(\changeurlcolor{black}%
				\href{http://creativecommons.org/licenses/by/4.0/.}%
				{http://creativecommons.org/licenses/by/4.0/}).}
				\end{minipage}
			}
		}
	}
	
%BOOK%
\newcommand{\Authorbox}[1]{\gdef\@Authorbox{#1}} %
\newcommand{\Titlebox}[1]{\gdef\@Titlebox{#1}}%
\newcommand{\Seriestitlebox}[1]{\gdef\@Seriestitlebox{#1}}%
\newcommand{\Editorbox}[1]{\gdef\@Editorbox{#1}} %
\newcommand{\Seriesnamebox}[1]{\gdef\@Seriesnamebox{#1}}%
\newcommand{\Serienumberbox}[1]{\gdef\@Seriesnumberbox{#1}}%
\newcommand{\Volumeumberbox}[1]{\gdef\@Volumenumberbox{#1}}%
\newcommand{\firstpagebox}[1]{\gdef\@firstpagebox{#1}}
\newcommand{\lastpagebox}[1]{\gdef\@lastpagebox{#1}} %

	
\newcommand{\citebox}{
\ifthenelse{\equal{\@arttype}{Book}}{
	\centerline{	
		\fbox{	
		\begin{minipage}[t][][t]{1.001\linewidth}
		\@Authorbox{} \@Titlebox. In \textit{\@Seriestitlebox}; \@Editorbox; \@Seriesnamebox{} \@Seriesnumberbox; MDPI: Basel, Switzerland, 2017; Vol. \@Volumenumberbox, pp. \@firstpagebox{} -- \@lastpagebox.
		\end{minipage}
		}
	}
}}{}
\endinput