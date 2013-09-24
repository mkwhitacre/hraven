/*
Copyright 2013 Twitter, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.twitter.hraven.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.util.Random;

import org.junit.Test;

public class TestByteArrayWrapper {
  @Test
  public void testGetPos() throws IOException {
    ByteArrayWrapper wrapper = createInstance(16);
    // read some bytes to move the position
    final int length = 4;
    byte[] buf = new byte[length];
    wrapper.read(buf);
    assertEquals(length, wrapper.getPos());
    wrapper.close();
  }

  @Test
  public void testSeek() throws IOException {
    ByteArrayWrapper wrapper = createInstance(16);
    // seek a new position
    final int position = 4;
    wrapper.seek(position);
    assertEquals(4, wrapper.getPos());
    wrapper.close();
  }

  @Test(expected=IOException.class)
  public void testSeekNegative() throws IOException {
    ByteArrayWrapper wrapper = createInstance(16);
    // seek a negative position
    wrapper.seek(-2);
    wrapper.close();
  }

  @Test(expected=IOException.class)
  public void testSeekOutOfBounds() throws IOException {
    ByteArrayWrapper wrapper = createInstance(16);
    // seek an out of bounds position
    wrapper.seek(20);
    wrapper.close();
  }

  @Test
  public void testSeekToNewSource() throws IOException {
    ByteArrayWrapper wrapper = createInstance(16);
    assertFalse(wrapper.seekToNewSource(1234));
    wrapper.close();
  }

  @Test
  public void testRead() throws IOException {
    byte[] array = createByteArray(16);
    ByteArrayWrapper wrapper = new ByteArrayWrapper(array);
    // get the current position
    final long oldPosition = wrapper.getPos();
    final long newPosition = 3;
    final int length = 4;
    byte[] buffer = new byte[length];
    int read = wrapper.read(newPosition, buffer, 0, length);
    // compare the contents
    assertEquals(length, read);
    compareByteArrays(array, buffer, (int) newPosition);
    // the position should not have changed
    assertEquals(oldPosition, wrapper.getPos());
    wrapper.close();
  }

  @Test
  public void testReadFully() throws IOException {
    // test the normal (positive) case
    byte[] array = createByteArray(16);
    ByteArrayWrapper wrapper = new ByteArrayWrapper(array);
    final long oldPosition = wrapper.getPos();
    final long newPosition = 3;
    final int length = 13;
    byte[] buffer = new byte[length];
    // call readFully
    wrapper.readFully(newPosition, buffer);
    // compare the contents
    compareByteArrays(array, buffer, (int) newPosition);
    // the position should not have changed
    assertEquals(oldPosition, wrapper.getPos());
    wrapper.close();
  }

  @Test(expected=IOException.class)
  public void testReadFullyInsufficientData() throws IOException {
    // go beyond what can be read
    ByteArrayWrapper wrapper = createInstance(16);
    byte[] buffer = new byte[8];
    try {
      wrapper.readFully(12, buffer);
    } finally {
      // make sure the position hasn't changed
      assertEquals(0, wrapper.getPos());
    }
  }

  /**
   * Creates an instance of <code>ByteArrayWrapper</code> for a randomly generated byte array of
   * the specified length.
   */
  private ByteArrayWrapper createInstance(int length) {
    return new ByteArrayWrapper(createByteArray(length));
  }

  private byte[] createByteArray(int length) {
    byte[] array = new byte[length];
    Random rng = new Random();
    rng.nextBytes(array);
    return array;
  }

  /**
   * Compares two byte arrays and asserts for equality. a from [offset, offset + b.length) is
   * compared with b.
   */
  private void compareByteArrays(byte[] a, byte[] b, int offset) {
    for (int i = 0; i < b.length; i++) {
      assertEquals(a[i + offset], b[i]);
    }
  }
}
