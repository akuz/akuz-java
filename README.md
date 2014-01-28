akuz-java-nlp-run-lda
=====================

Executable jar and shell scripts for running optimised 
LDA Gibbs sampling on a directory of text files.

This project depends on other `akuz` libraries:

  * `akuz-java-core`
  * `akuz-java-nlp`
  
Releases already contain relevant binaries.

### LDA Optimisations

The optimised Gibbs sampling inference for LDA provides 
the following improvements over the standard approach.

#### Multiprocessor Parallelisation 

Benefit: faster inference on machines with multiple processors.

The calculations necessary for each Gibbs sampling
iteration are executed on different threads by splitting
the corpus into non-overlapping document sets, and after
each iteration has completed, merging the results, which
is a negligible operation compared to sampling.

#### Simulated Annealing 

Benefit: faster convergence of Gibbs sampling.

The "temperature" parameter influences the ratio of prior
Dirichlet mass to mass mass of the data being added to prior. 
At a high temperature of 1.0 the Dirichlet priors will be modified 
to be approximatelly of the same mass as the observed data mass. 
This allows sampling to initially explore wider areas of the 
posterior space. Then, by lowering the temperature in steps 
(after a number of iterations with each temperature), we 
gradually reduce sampling space exploration variance
and converge onto the area of highest probability.
This process converges on topics much faster than
sampling with a constant (low) temperature, which
is what happens when you sample with the target
values of hyperparameters from the start.

#### Asymmetric Word Priors

Benefit: option to specify keywords for individual topics.

You might wish to prioritise certain keywords to have higher
probability in specific topics. This might be necessary in order
to infer the topics that you wish to use further down the line,
for example for detecting these topics in the new documents.

### Running the Inference

Each release contains an attached zip file with the binaries.

The executable script is `akuz-java-nlp-run-lda.sh`

You might need to make it runnable on your system:

`chmod a+x akuz-java-nlp-run-lda.sh`

Then edit the file to specify the parameters:

```ARGUMENTS:
   -inputDir string               : Directory containing input text files
   -outputDir string              : Directory where to save output files
 [ -stopWordsFile string        ] : File with stop words to ignore (default none)
 [ -topicCount int              ] : Number of topics for LDA inference (default 10)
 [ -topicOutputStemsCount int   ] : Number of stems to output for each topic (default 100)
 [ -noiseTopicProportion double ] : Noise topic proportion (default 0.5)
 [ -threadCount int             ] : Number of threads to use (default 2)
 [ -burnInStartTemp int         ] : Burn in start temperature (default 1.0)
 [ -burnInEndTemp int           ] : Burn in end temperature (default 0.1)
 [ -burnInTempDecay double      ] : Burn in temperature decay (default 0.75)
 [ -burnInTempIter int          ] : Burn in iterations count per temperature (default 10)
 [ -samplingIter int            ] : Sampling iterations count (default 100)
```

Then run it as follows:

`./akuz-java-nlp-run-lda.sh`

### Resources

To read relevant blog articles or to get the test data,
please see <http://akuz.me>
