# GuideLine Navigator - GuLiNav
GuLiNav lets you infer context-specific representations of computerized clinical practice
guidelines. It follows the 3-Layer approach as described [HERE](https://doi.org/10.3233/SHTI190815).
It focuses on implementing the Guideline Procedures Layer (second Layer) using BPMN-based procedure
models and provides a simple variant of the Medical Criteria Layer (first layer). It can be
connected to an implementation of the Institution Workflow Layer (third layer) directly in code 
by using it as a software library. More sophisticated interpreters for the Medical Criteria Layer 
(e.g. an ARDEN Syntax Engine) can be registered and used instead of (or in addition to) the 
aforementioned simplistic variant that is included in GuLiNav.

The implementation also provides a simplistic REST-based interface to interpret previously 
defined practice guidelines. The code comes with an artificial mini-example (adapted from a clinical
algorithm for the treatment of specific bloodstream infections
[see Here](https://doi.org/10.1136/bmjopen-2019-033391)), but custom guidelines can also be uploaded
into the backend using the REST interface. 

_The REST Interface is mainly for demonstration  
purposes, since the software is meant to be used as a library in combination with an implementation
of the other two layers._

## Get started
GuLiNav is a software library, but if you want to have a peek at its capabilities, you can use a 
simple demonstrator frontend for a preview.

You can start the demonstrator-frontend using Gradle's `bootRun` task of the gulinav-rest module:

```
./gradlew build gulinav-rest:bootRun
```
The system will be available on ```localhost:8080```.

### Use docker version
In case you want to use the dockerized version of GuLiNav (e.g. because you do not have Java 11
installed), you can just build the docker image and then run it afterwards. Make sure to map the
correct port:

```
docker build -t gulinav .
docker container run -p 8080:8080 gulinav
```

Here the functionality of GuLiNav can be accessed via a simplistic demonstrator user-interface.

## Using GuLiNav as a software library (example)
GuLiNav currently provides two modules that can be used as a software library: `core` and `mks`.
The `gulinav-cli` is an example project uses GuLiNav as a software library to make GuLiNav usable 
from the command-line. It is the most basic example to show how GuLiNav can be used that way:

1. Create an input-stream to the CIG
```
    final InputStream bpmnIs = // Input Stream to BPMN File.    
```
2. Create a repository to store the CIGs
```
    final GuidelineProcessorRepository repo = new GuidelineProcessorRepository();
```
3. Add the CIG to the repository
```
    // Add the guideline to the repo under a random, yet known UUID
    final String randomUUID = UUID.randomUUID().toString();
    repo.addGuideline(randomUUID, bpmnIs);
```
4. Read the patient data / context data
```
    // TODO: Here we would need to read the patient-data from the command line or define another way
    //  to provide the patient-data to the processor. For now, we pretend we know nothing about the
    //  patient (thus usually the very first step of the guideline should be active etc.)
    final SimplePatient patientData = new SimplePatient();
```
5. Process the guideline
```
    final List<GuidelineItem> contextualizedGuideline = repo.process(randomUUID, patientData);

```
6. Do something with the result
```
    System.out.println("The resulting guideline-items are: ");
    System.out.println("#########################################################################");
    contextualizedGuideline.forEach(System.out::println);
```
The output of this procedure looks like this for the included demo file:
```
The resulting guideline-items are: 
#########################################################################
GuidelineItem(id=null, externalId=null, taskId=Task_1vc7eqj, description=read microbiology report, detailText=null, timePhase=PRESENT, handledProperties={AUREUS_OR_LUGDUNENSIS=MISC:AUREUS_OR_LUGDUNENSIS}, properties={AUREUS_OR_LUGDUNENSIS=null}, performer=HUMAN, basedOnExternalEntity=null)
GuidelineItem(id=null, externalId=null, taskId=Task_0p2sfeg, description=consult an infectiologist, detailText=null, timePhase=FUTURE, handledProperties={INFECTIOLOGIST=MISC:INFECTIOLOGIST}, properties={INFECTIOLOGIST=null}, performer=HUMAN, basedOnExternalEntity=null)
GuidelineItem(id=null, externalId=null, taskId=tInvestigate, description=investigate if more than one sample positive, detailText=null, timePhase=FUTURE, handledProperties={MULTIPLE_SAMPLES=MISC:MULTIPLE_SAMPLES}, properties={MULTIPLE_SAMPLES=null}, performer=DEVICE, basedOnExternalEntity=null)
GuidelineItem(id=null, externalId=null, taskId=tDecideSampleIndependence, description=decide if samples are sufficiently independent , detailText=null, timePhase=FUTURE, handledProperties={SUFFICIENTLY_INDEPENDENT=MKM:SUFFICIENTLY_INDEPENDENT}, properties={SUFFICIENTLY_INDEPENDENT=null}, performer=DEVICE, basedOnExternalEntity=null)
GuidelineItem(id=null, externalId=null, taskId=tTakeBloodsample, description=take another blood sample, detailText=null, timePhase=FUTURE, handledProperties={ANOTHER_SAMPLE_TAKEN=MISC:ANOTHER_SAMPLE_TAKEN}, properties={ANOTHER_SAMPLE_TAKEN=null}, performer=HUMAN, basedOnExternalEntity=null)
GuidelineItem(id=null, externalId=null, taskId=Task_05tug1u, description=decide if independent samples are both positive, detailText=null, timePhase=FUTURE, handledProperties={BOTH_POSITIVE=MISC:BOTH_POSITIVE}, properties={BOTH_POSITIVE=null}, performer=DEVICE, basedOnExternalEntity=null)
GuidelineItem(id=null, externalId=null, taskId=tSearchFoci, description=search for potential infection foci, detailText=null, timePhase=FUTURE, handledProperties={FOCI_EXIST=MISC:FOCI_EXIST}, properties={FOCI_EXIST=null}, performer=HUMAN, basedOnExternalEntity=null)
GuidelineItem(id=null, externalId=null, taskId=tRemoveFoci, description=treat foci, detailText=null, timePhase=FUTURE, handledProperties={REMOVE_FOCI=MISC:REMOVE_FOCI}, properties={REMOVE_FOCI=null}, performer=HUMAN, basedOnExternalEntity=null)
GuidelineItem(id=null, externalId=null, taskId=tAntibioticTreatment, description=antibiotic treatment, detailText=null, timePhase=FUTURE, handledProperties={ANTIBIOTIC_TREATMENT=MISC:ANTIBIOTIC_TREATMENT}, properties={ANTIBIOTIC_TREATMENT=null}, performer=HUMAN, basedOnExternalEntity=null)

```

Note that this is purely for the sake of demonstrating how to use the GuLiNav library.
There is no real use-case for such a command-line tool and also the usage of fat-jars should 
generally be discouraged.


### core
The core module contains the most relevant part of GuLiNav and can be used by adding a guideline to
the repository:
```
processorRepository.addGuideline("demo", inputStreamToBpmnModel);
```
For a more details consult the example above (GuLiNav via CLI).

### mks
The mks module (mks: medical knowledge service) acts as a simple java framework to encode 
medical knowledge (as could otherwise, e.g. be encoded as an ARDEN Syntax MLM).
The MKS is a technical service which requires the knowledge to be defined in natural language 
first, since it is inaccessible for non-technicians. It is a simplistic implementation of the 
medical criteria layer which is registered into GuLiNav `core` by default. 

#### adding other knowledge evaluator
The mks implements the `PatientEvaluator`-Interface of the `core` module and is consequently 
registered into the core module for "MKM" system.
You should implement the `PatientEvaluator`-interface for an ARDEN Engine (or any other
knowledge-evaluator) and register it in the core module using another system.


## REST Interface
The interface provides two simple POST Resources for the sake of demonstrating GuLiNav's mode of 
operation.

### POST /guideline
This resource allows it to upload new (versions of) guidelines to GuLiNav. The exact constraints for
the structure of this BPMN-based guideline definition are omitted in this README. An example is
already uploaded by default. It is contained in the `core` module's resources (demo.bpmn)

Example Payload:

```json
{
  "id": "some_id_1234",
  "guideline": "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n........ REPR. OF BPMN"
}
```

### POST /evaluatePatient
This resource allows it to combine a previously uploaded guideline with patient data . In addition,
a guideline with id _demo_ is already present by default (and used as an example within this readme
).

```json
{
  "guidelineId": "demo",
  "patientPayload": [
    {
      "code": "MISC:AUREUS_OR_LUGDUNENSIS",
      "values": [
        {
          "time": "2020-12-02T09:49:49.401Z",
          "value": false
        }
      ]
    },
    {
      "code": "MISC:MULTIPLE_SAMPLES",
      "values": [
        {
          "time": "2020-12-02T09:49:49.401Z",
          "value": false
        }
      ]
    },
    {
      "code": "MISC:ANOTHER_SAMPLE_TAKEN",
      "values": [
        {
          "time": "2020-12-02T09:49:49.401Z",
          "value": true
        }
      ]
    }
  ]
}
```

The evaluation is done while taking any patient data that is provided into account.
