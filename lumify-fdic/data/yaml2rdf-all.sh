#!/bin/bash

for y in *.yaml; do ./yaml2rdf.rb $y; done;

